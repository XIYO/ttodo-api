package point.ttodoApi.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.*;
import point.ttodoApi.test.BaseIntegrationTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@DisplayName("JWT 토큰 HTTP 통합 테스트")
public class JwtTokenHttpTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String validJwtToken;
    private String expiredJwtToken;
    private String invalidJwtToken;

    @BeforeEach
    public void setUp() throws Exception {
        // 유효한 JWT 토큰 (익명 사용자)
        validJwtToken = generateValidToken();
        
        // 만료된 JWT 토큰
        expiredJwtToken = generateExpiredToken();
        
        // 잘못된 서명의 JWT 토큰
        invalidJwtToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJmYWtlLXVzZXIiLCJpYXQiOjE3NTY0OTcyMDQsImV4cCI6NDkxMDA5NzIwNCwiZW1haWwiOiJmYWtlQHRlc3QuY29tIn0.invalid_signature";
    }

    @Test
    @DisplayName("유효한 Bearer 토큰으로 TODO 목록 조회 성공")
    public void testValidBearerToken() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + validJwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("토큰 없이 요청 시 401 Unauthorized")
    public void testNoToken() throws Exception {
        mockMvc.perform(get("/todos"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("만료된 토큰으로 요청 시 401 Unauthorized")
    public void testExpiredToken() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + expiredJwtToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 서명의 토큰으로 요청 시 401 Unauthorized")
    public void testInvalidSignatureToken() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + invalidJwtToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Bearer 접두사 없는 토큰으로 요청 시 401 Unauthorized")
    public void testTokenWithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", validJwtToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 토큰으로 TODO 통계 조회 성공")
    public void testTodoStatistics() throws Exception {
        mockMvc.perform(get("/todos/statistics")
                .header("Authorization", "Bearer " + validJwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").exists())
                .andExpect(jsonPath("$.completedCount").exists());
    }

    @Test
    @DisplayName("유효한 토큰으로 프로필 조회 성공")
    public void testGetProfile() throws Exception {
        mockMvc.perform(get("/profiles/me")
                .header("Authorization", "Bearer " + validJwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("ffffffff-ffff-ffff-ffff-ffffffffffff"))
                .andExpect(jsonPath("$.email").value("anon@ttodo.dev"))
                .andExpect(jsonPath("$.nickname").value("익명사용자"));
    }

    @Test
    @DisplayName("유효한 토큰으로 카테고리 목록 조회 성공")
    public void testGetCategories() throws Exception {
        mockMvc.perform(get("/categories")
                .header("Authorization", "Bearer " + validJwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("유효한 토큰으로 TODO 생성 성공")
    public void testCreateTodo() throws Exception {
        String todoJson = """
            {
                "title": "테스트 TODO",
                "description": "통합 테스트로 생성된 TODO",
                "date": "2025-08-30",
                "priorityId": 1,
                "categoryId": "f8ee5e07-3956-466f-9370-a8c75f916b5a",
                "tags": ["테스트", "자동화"]
            }
            """;
        
        mockMvc.perform(post("/todos")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(todoJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    // 헬퍼 메서드: 유효한 토큰 생성
    private String generateValidToken() throws Exception {
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
        long exp = iat + (365L * 100 * 24 * 60 * 60); // 100년
        
        String payload = String.format(
            "{\"sub\":\"ffffffff-ffff-ffff-ffff-ffffffffffff\",\"iat\":%d,\"exp\":%d,\"email\":\"anon@ttodo.dev\",\"nickname\":\"익명사용자\",\"timeZone\":\"Asia/Seoul\",\"locale\":\"ko_KR\",\"scope\":\"ROLE_USER\"}",
            iat, exp
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

    // 헬퍼 메서드: 만료된 토큰 생성
    private String generateExpiredToken() throws Exception {
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
        
        long iat = System.currentTimeMillis() / 1000 - 86400; // 1일 전
        long exp = iat + 3600; // 1시간 후 (이미 만료됨)
        
        String payload = String.format(
            "{\"sub\":\"ffffffff-ffff-ffff-ffff-ffffffffffff\",\"iat\":%d,\"exp\":%d,\"email\":\"anon@ttodo.dev\",\"nickname\":\"익명사용자\",\"timeZone\":\"Asia/Seoul\",\"locale\":\"ko_KR\",\"scope\":\"ROLE_USER\"}",
            iat, exp
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