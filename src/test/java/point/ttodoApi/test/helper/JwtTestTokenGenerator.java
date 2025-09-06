package point.ttodoApi.test.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 테스트용 JWT 토큰 생성 유틸리티
 * 실제 RSA 키로 서명된 유효한 JWT 토큰을 생성합니다.
 */
@Component
public class JwtTestTokenGenerator {

  @Autowired
  private JwtEncoder jwtEncoder;

  /**
   * anon@ttodo.dev 사용자용 영구 토큰 생성
   * 100년 후 만료되는 사실상 영구 토큰입니다.
   */
  public String generateAnonUserToken() {
    Instant now = Instant.now();

    // 100년 후 만료 (사실상 영구)
    Instant expiresAt = now.plus(36500, ChronoUnit.DAYS);

    JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject("ffffffff-ffff-ffff-ffff-ffffffffffff")  // anon user ID
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("email", "anon@ttodo.dev")
            .claim("nickname", "익명사용자")
            .claim("timeZone", "Asia/Seoul")
            .claim("locale", "ko-KR")
            .claim("scope", "ROLE_USER")
            .build();

    JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).keyId("rsa-key-id").build(),
            claims
    );

    return jwtEncoder.encode(parameters).getTokenValue();
  }

  /**
   * 특정 사용자용 토큰 생성
   */
  public String generateTokenForUser(String userId, String email, String nickname) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(36500, ChronoUnit.DAYS);

    JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(userId)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("email", email)
            .claim("nickname", nickname)
            .claim("timeZone", "Asia/Seoul")
            .claim("locale", "ko-KR")
            .claim("scope", "ROLE_USER")
            .build();

    JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).keyId("rsa-key-id").build(),
            claims
    );

    return jwtEncoder.encode(parameters).getTokenValue();
  }

  /**
   * 만료된 토큰 생성 (테스트용)
   */
  public String generateExpiredToken() {
    Instant now = Instant.now();

    // 1시간 전에 만료
    Instant expiresAt = now.minus(1, ChronoUnit.HOURS);

    JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject("ffffffff-ffff-ffff-ffff-ffffffffffff")
            .issuedAt(now.minus(2, ChronoUnit.HOURS))
            .expiresAt(expiresAt)
            .claim("email", "anon@ttodo.dev")
            .claim("nickname", "익명사용자")
            .claim("timeZone", "Asia/Seoul")
            .claim("locale", "ko-KR")
            .claim("scope", "ROLE_USER")
            .build();

    JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).keyId("rsa-key-id").build(),
            claims
    );

    return jwtEncoder.encode(parameters).getTokenValue();
  }
}