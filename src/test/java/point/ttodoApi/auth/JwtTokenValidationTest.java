package point.ttodoApi.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT 토큰 검증 단위 테스트")
public class JwtTokenValidationTest {

    private static final String ANON_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final String ANON_EMAIL = "anon@ttodo.dev";
    private static final String ANON_NICKNAME = "익명사용자";
    
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private String hardcodedToken;

    @BeforeEach
    public void setUp() throws Exception {
        // 개인키 로드
        String privateKeyPath = "src/main/resources/ttodo/jwt/test-private.pem";
        String privateKeyContent = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        privateKeyContent = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateSpec);
        
        // 공개키 로드
        String publicKeyPath = "src/main/resources/ttodo/jwt/public.pem";
        String publicKeyContent = new String(Files.readAllBytes(Paths.get(publicKeyPath)));
        publicKeyContent = publicKeyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = (RSAPublicKey) keyFactory.generatePublic(publicSpec);
        
        // README에 있는 하드코딩된 토큰
        hardcodedToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJmZmZmZmZmZi1mZmZmLWZmZmYtZmZmZi1mZmZmZmZmZmZmZmYiLCJpYXQiOjE3NTY0OTcyMDQsImV4cCI6NDkxMDA5NzIwNCwiZW1haWwiOiJhbm9uQHR0b2RvLmRldiIsIm5pY2tuYW1lIjoi7J2166qF7IKs7Jqp7J6QIiwidGltZVpvbmUiOiJBc2lhL1Nlb3VsIiwibG9jYWxlIjoia29fS1IiLCJzY29wZSI6IlJPTEVfVVNFUiJ9.0omjGk_61raPaG4yof4tLGInII276NkzdS1rjRhf9erzXRFjMvQsbl-FAFWdll5l6YPEbmoSVLoXzCqDJU4X_fXhC6bAEUXIs4_2_IrgsxxpoWGC_KaTv6tCd-35EPb12AfSTkLHpaXlUjbmEkNiAZypD54ICfUY_6f3ts0Ki75GFjLJ0wGUju7vX8ECHljxLhyNt6H1XVgKGUxta1Fx_R1wcaiJZR0j0I7LW0JV3ZRbO1hG_3in9Y3eL5k-hYRSYLXJr6H6GNzY2ztbKru2tXVRJQFuGVrsx-RPzNmm-L5xb-DBRFrt6KDa1bQoedL12WgFTWwQe96Uk-DhoOyPhw";
    }

    @Test
    @DisplayName("하드코딩된 토큰의 서명 검증")
    public void testHardcodedTokenSignature() throws Exception {
        String[] parts = hardcodedToken.split("\\.");
        assertEquals(3, parts.length, "JWT는 3개의 부분으로 구성되어야 합니다");
        
        // 서명 검증
        String message = parts[0] + "." + parts[1];
        byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);
        
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(message.getBytes());
        
        assertTrue(signature.verify(signatureBytes), "토큰 서명이 유효해야 합니다");
    }

    @Test
    @DisplayName("하드코딩된 토큰의 페이로드 검증")
    public void testHardcodedTokenPayload() throws Exception {
        String[] parts = hardcodedToken.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        
        // 페이로드에 필수 클레임이 포함되어 있는지 확인
        assertTrue(payloadJson.contains("\"sub\":\"" + ANON_USER_ID + "\""), 
                  "페이로드에 올바른 사용자 ID가 포함되어야 합니다");
        assertTrue(payloadJson.contains("\"email\":\"" + ANON_EMAIL + "\""), 
                  "페이로드에 올바른 이메일이 포함되어야 합니다");
        assertTrue(payloadJson.contains("\"nickname\":\"익명사용자\""), 
                  "페이로드에 올바른 닉네임이 포함되어야 합니다");
        assertTrue(payloadJson.contains("\"scope\":\"ROLE_USER\""), 
                  "페이로드에 올바른 권한이 포함되어야 합니다");
    }

    @Test
    @DisplayName("하드코딩된 토큰의 만료 시간 검증")
    public void testHardcodedTokenExpiration() throws Exception {
        String[] parts = hardcodedToken.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        
        // exp 클레임 추출
        int expIndex = payloadJson.indexOf("\"exp\":");
        assertTrue(expIndex > 0, "exp 클레임이 존재해야 합니다");
        
        int expValueStart = expIndex + 6;
        int expValueEnd = payloadJson.indexOf(",", expValueStart);
        if (expValueEnd == -1) {
            expValueEnd = payloadJson.indexOf("}", expValueStart);
        }
        
        String expValue = payloadJson.substring(expValueStart, expValueEnd);
        long exp = Long.parseLong(expValue);
        
        // 100년 후 (대략적으로) - 2125년 1월 1일 이후
        long year2125 = 4891000000L; // 2125년 1월 1일 UTC
        assertTrue(exp >= year2125, "토큰은 100년 후(2125년 이후)에 만료되어야 합니다");
    }

    @Test
    @DisplayName("새로 생성한 토큰 검증")
    public void testNewlyGeneratedToken() throws Exception {
        // 새 토큰 생성
        String newToken = generateToken();
        
        // 토큰 구조 검증
        String[] parts = newToken.split("\\.");
        assertEquals(3, parts.length, "JWT는 3개의 부분으로 구성되어야 합니다");
        
        // 서명 검증
        String message = parts[0] + "." + parts[1];
        byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);
        
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(message.getBytes());
        
        assertTrue(signature.verify(signatureBytes), "새로 생성한 토큰의 서명이 유효해야 합니다");
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 검증 실패")
    public void testInvalidSignature() throws Exception {
        String invalidToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJmYWtlLXVzZXIifQ.invalid_signature";
        
        String[] parts = invalidToken.split("\\.");
        assertEquals(3, parts.length);
        
        // 서명 검증은 실패해야 함
        assertThrows(Exception.class, () -> {
            String message = parts[0] + "." + parts[1];
            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());
            signature.verify(signatureBytes);
        });
    }

    @Test
    @DisplayName("토큰 헤더 검증")
    public void testTokenHeader() throws Exception {
        String[] parts = hardcodedToken.split("\\.");
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
        
        assertTrue(headerJson.contains("\"alg\":\"RS256\""), 
                  "헤더에 RS256 알고리즘이 명시되어야 합니다");
        assertTrue(headerJson.contains("\"kid\":\"rsa-key-id\""), 
                  "헤더에 키 ID가 포함되어야 합니다");
        assertTrue(headerJson.contains("\"typ\":\"JWT\""), 
                  "헤더에 JWT 타입이 명시되어야 합니다");
    }

    // 헬퍼 메서드: 토큰 생성
    private String generateToken() throws Exception {
        String header = "{\"alg\":\"RS256\",\"kid\":\"rsa-key-id\",\"typ\":\"JWT\"}";
        String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
        
        long iat = System.currentTimeMillis() / 1000;
        long exp = iat + (365L * 100 * 24 * 60 * 60);
        
        String payload = String.format(
            "{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d,\"email\":\"%s\",\"nickname\":\"%s\",\"timeZone\":\"Asia/Seoul\",\"locale\":\"ko_KR\",\"scope\":\"ROLE_USER\"}",
            ANON_USER_ID, iat, exp, ANON_EMAIL, ANON_NICKNAME
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
}