package point.ttodoApi.test.helper;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * 테스트용 인증 헬퍼 클래스
 * JWT 토큰 생성 및 인증 관련 유틸리티 제공
 */
@Component
public class TestAuthHelper {
    
    // 테스트용 익명 사용자 정보
    public static final UUID ANON_USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    public static final String ANON_USER_EMAIL = "anon@ttodo.dev";
    public static final String ANON_USER_NICKNAME = "익명사용자";
    
    /**
     * 테스트용 유효한 JWT 토큰 생성
     */
    public static String generateValidToken() throws Exception {
        return generateToken(ANON_USER_ID.toString(), ANON_USER_EMAIL, ANON_USER_NICKNAME, false);
    }
    
    /**
     * 만료된 JWT 토큰 생성
     */
    public static String generateExpiredToken() throws Exception {
        return generateToken(ANON_USER_ID.toString(), ANON_USER_EMAIL, ANON_USER_NICKNAME, true);
    }
    
    /**
     * 커스텀 JWT 토큰 생성
     */
    public static String generateToken(String userId, String email, String nickname, boolean expired) throws Exception {
        String privateKeyPath = "src/main/resources/ttodo/jwt/test-private.pem";
        String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        
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
            exp = iat + (365L * 100 * 24 * 60 * 60); // 100년
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
     * 하드코딩된 익명 사용자 토큰 (README에 있는 토큰)
     */
    public static String getHardcodedAnonToken() {
        return "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJmZmZmZmZmZi1mZmZmLWZmZmYtZmZmZi1mZmZmZmZmZmZmZmYiLCJpYXQiOjE3NTY0OTcyMDQsImV4cCI6NDkxMDA5NzIwNCwiZW1haWwiOiJhbm9uQHR0b2RvLmRldiIsIm5pY2tuYW1lIjoi7J2166qF7IKs7Jqp7J6QIiwidGltZVpvbmUiOiJBc2lhL1Nlb3VsIiwibG9jYWxlIjoia29fS1IiLCJzY29wZSI6IlJPTEVfVVNFUiJ9.0omjGk_61raPaG4yof4tLGInII276NkzdS1rjRhf9erzXRFjMvQsbl-FAFWdll5l6YPEbmoSVLoXzCqDJU4X_fXhC6bAEUXIs4_2_IrgsxxpoWGC_KaTv6tCd-35EPb12AfSTkLHpaXlUjbmEkNiAZypD54ICfUY_6f3ts0Ki75GFjLJ0wGUju7vX8ECHljxLhyNt6H1XVgKGUxta1Fx_R1wcaiJZR0j0I7LW0JV3ZRbO1hG_3in9Y3eL5k-hYRSYLXJr6H6GNzY2ztbKru2tXVRJQFuGVrsx-RPzNmm-L5xb-DBRFrt6KDa1bQoedL12WgFTWwQe96Uk-DhoOyPhw";
    }
}