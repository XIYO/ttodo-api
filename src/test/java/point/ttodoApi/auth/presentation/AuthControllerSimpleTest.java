package point.ttodoApi.auth.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import point.ttodoApi.auth.application.AuthCommandService;
import point.ttodoApi.auth.application.AuthQueryService;
import point.ttodoApi.auth.application.command.SignUpCommand;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.auth.presentation.mapper.AuthPresentationMapper;
import point.ttodoApi.common.fixture.AuthFixtures;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AuthController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("AuthController Simple Test")
class AuthControllerSimpleTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthCommandService authCommandService;

    @MockitoBean
    private AuthQueryService authQueryService;

    @MockitoBean
    private AuthPresentationMapper authMapper;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;
    
    // ===== Validation 관련 추가 의존성 Mock =====
    @MockitoBean
    private UserRepository userRepository; // @UniqueEmailValidator 가 참조

    @Test
    @DisplayName("회원가입 간단 테스트 - JSON")
    void simpleSignUpTest() throws Exception {
    // Given
    AuthResult result = AuthFixtures.createAuthResult();
    given(authCommandService.signUp(any(SignUpCommand.class))).willReturn(result);
    given(authMapper.toCommand(any(), any(), any())).willReturn(
        new SignUpCommand("test@example.com", "Password123!", "테스트유저", null, "device")
    );
    // JSON 본문 구성 (record 매핑)
    String jsonBody = new ObjectMapper().writeValueAsString(
        java.util.Map.of(
            "email", "test@example.com",
            "password", "Password123!",
            "confirmPassword", "Password123!",
            "nickname", "테스트유저",
            "introduction", "",
            "timeZone", "Asia/Seoul",
            "locale", "ko_KR"
        )
    );

    // When & Then
    mockMvc.perform(post("/auth/sign-up")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody))
        .andDo(print())
        .andExpect(status().isOk());
    }
}