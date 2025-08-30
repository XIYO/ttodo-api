package point.ttodoApi.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.test.BaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security 설정을 검증하는 통합 테스트
 * 실제 JWT 토큰을 사용하여 인증/인가를 테스트합니다.
 */
@AutoConfigureMockMvc
@Transactional
@DisplayName("Security 통합 테스트")
public class SecurityIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    // generateAndPrintAnonToken 테스트로 생성한 하드코딩된 토큰
    private static final String ANON_JWT_TOKEN = "eyJraWQiOiJ0ZXN0LXJzYS1rZXktaWQiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJmZmZmZmZmZi1mZmZmLWZmZmYtZmZmZi1mZmZmZmZmZmZmZmYiLCJzY29wZSI6IlJPTEVfVVNFUiIsIm5pY2tuYW1lIjoi7J2166qF7IKs7Jqp7J6QIiwidGltZVpvbmUiOiJBc2lhL1Nlb3VsIiwibG9jYWxlIjoia28tS1IiLCJpYXQiOjE3NTY1Mzc2OTUsImVtYWlsIjoiYW5vbkB0dG9kby5kZXYifQ.bB0M8VUv-h28bryRqGe2o8Z8zzWtUnZAAdgq31RNN8mLBMyGKsp4eNZq1Mdf0JBNiNdQTlLowsav1b102ViFdbocL1ZumBYLLRt8MWZ5NTh_xa73lqtcyDPWgdFS1XGknvKV6naiRRBPrYWfPT-RPhQ_GOxA5TXdLC_MHap5Mi_ui3EYig8g1_Dcz9bnIuFz76LsoA5FtwgkN5Y71_S36APdDGrZIqLmcA9eQuopkV2jWCpedSZ9fJkKWJcQuA4J5rjZ36Rr-__DWtfDYSogGWzIw9XLIIDi6UEbeXyK1GVgWaOve5Z5IeUQN1Fe3ZxwcDqazS01xnIa73qFv3M_nQ";
    
    @Test
    @DisplayName("인증 없이 보호된 엔드포인트 접근 시 401 반환")
    void shouldReturn401WhenAccessingProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/todos"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("JWT 토큰으로 보호된 엔드포인트 접근 성공")
    void shouldAccessProtectedEndpointWithJwtToken() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + ANON_JWT_TOKEN))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("잘못된 JWT 토큰으로 접근 시 401 반환")
    void shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer invalid_token"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("/auth/dev-token은 인증 없이 접근 가능 (permitAll)")
    void devTokenEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/auth/dev-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.userId").value("ffffffff-ffff-ffff-ffff-ffffffffffff"));
    }
    
    @Test
    @DisplayName("/auth/sign-up은 인증 없이 접근 가능 (permitAll)")
    void signUpEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("passwordConfirm", "password123")
                .param("nickname", "테스트유저"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("/auth/sign-in은 인증 없이 접근 가능 (permitAll)")
    void signInEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        // 먼저 회원가입
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "signin@example.com")
                .param("password", "password123")
                .param("passwordConfirm", "password123")
                .param("nickname", "로그인테스트"));
        
        // 로그인
        mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "signin@example.com")
                .param("password", "password123"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("실제 로그인 후 받은 JWT 토큰으로 API 접근")
    void shouldAccessApiWithRealJwtFromSignIn() throws Exception {
        // 로그인하여 JWT 토큰 받기
        MvcResult loginResult = mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "anon@ttodo.dev")
                .param("password", ""))
            .andExpect(status().isOk())
            .andReturn();
        
        // 응답 헤더에서 Authorization 토큰 추출
        String authHeader = loginResult.getResponse().getHeader("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Bearer ");
        
        String token = authHeader.substring(7); // "Bearer " 제거
        
        // 받은 토큰으로 API 접근
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("JWT 토큰으로 Todo 생성")
    void shouldCreateTodoWithJwtToken() throws Exception {
        mockMvc.perform(post("/todos")
                .header("Authorization", "Bearer " + ANON_JWT_TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "JWT로 생성한 할일"))
            .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("Bearer 프리픽스 없이 토큰 전달 시 401")
    void shouldReturn401WithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", ANON_JWT_TOKEN)) // Bearer 없이
            .andExpect(status().isUnauthorized());
    }
}