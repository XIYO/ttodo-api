package point.ttodoApi.test.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * JWT 토큰 테스트용 헬퍼 클래스
 * RSA 키를 사용한 JWT 토큰 생성 유틸리티
 * 토큰 만료 시간은 설정 파일에서 관리
 */
@Component
public class JwtTokenTestHelper {

  private static final String ANON_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
  private static final String ANON_EMAIL = "anon@ttodo.dev";
  private static final String ANON_NICKNAME = "익명사용자";

  @Value("${test.auth.token-expiration-seconds:86400}")
  private long tokenExpirationSeconds;

  /**
   * 테스트용 유효한 JWT 토큰 생성
   */
  public String generateValidToken() throws Exception {
    return generateToken(ANON_USER_ID, ANON_EMAIL, ANON_NICKNAME, false);
  }

  /**
   * 만료된 JWT 토큰 생성
   */
  public String generateExpiredToken() throws Exception {
    return generateToken(ANON_USER_ID, ANON_EMAIL, ANON_NICKNAME, true);
  }

  /**
   * 커스텀 JWT 토큰 생성
   */
  public String generateToken(String userId, String email, String nickname, boolean expired) throws Exception {
    ClassPathResource resource = new ClassPathResource("ttodo/jwt/test-private.pem");
    String privateKeyContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

    privateKeyContent = privateKeyContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

    byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(spec);

    String header = "{\"alg\":\"RS256\",\"kid\":\"rsa-key-id\",\"typ\":\"JWT\"}";
    String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());

    long iat = System.currentTimeMillis() / 1000;
    long exp;

    if (expired) {
      iat = iat - 86400; // 1일 전
      exp = iat + 3600; // 1시간 후 (이미 만료됨)
    } else {
      exp = iat + tokenExpirationSeconds; // 설정 파일에서 정의한 만료 시간 사용
    }

    String payload = String.format(
            "{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d,\"email\":\"%s\",\"nickname\":\"%s\",\"timeZone\":\"Asia/Seoul\",\"locale\":\"ko_KR\",\"scope\":\"ROLE_USER\"}",
            userId, iat, exp, email, nickname
    );
    String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

    String message = encodedHeader + "." + encodedPayload;
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(privateKey);
    signature.update(message.getBytes());
    byte[] signatureBytes = signature.sign();
    String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

    return message + "." + encodedSignature;
  }

  /**
   * 개발/테스트용 익명 사용자 토큰
   * 이 토큰은 개발 환경에서만 사용해야 하며, 프로덕션에서는 사용하지 마세요
   * 토큰 만료 시간은 application.yml의 test.auth.token-expiration-seconds로 설정
   *
   * @deprecated 개발용 토큰은 generateValidToken()을 사용하세요
   */
  @Deprecated
  public String getDevelopmentToken() throws Exception {
    // 개발용 토큰은 동적으로 생성하여 환경 변수의 만료 시간을 따르도록 함
    return generateValidToken();
  }
}