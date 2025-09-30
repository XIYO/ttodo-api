package point.ttodoApi.auth.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.auth.application.AuthCommandService;
import point.ttodoApi.auth.application.command.SignUpCommand;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.auth.presentation.mapper.AuthPresentationMapper;
import point.ttodoApi.common.fixture.AuthFixtures;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.validation.sanitizer.ValidationUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private AuthPresentationMapper authMapper;
    
    @MockitoBean
    private ValidationUtils validationUtils;
    
    @MockitoBean
    private CookieService cookieService;

    @Test
    @DisplayName("회원가입 간단 테스트")
    void simpleSignUpTest() throws Exception {
        // Given
        AuthResult result = AuthFixtures.createAuthResult();
        given(authCommandService.signUp(any(SignUpCommand.class))).willReturn(result);
        given(authMapper.toCommand(any(), any(), any())).willReturn(
            new SignUpCommand("test@example.com", "password", "nickname", null, "device")
        );
        given(validationUtils.sanitizeHtmlStrict(any())).willAnswer(i -> i.getArgument(0));
        given(validationUtils.sanitizeHtml(any())).willAnswer(i -> i.getArgument(0));
        
        // When & Then
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!")
                .param("nickname", "테스트유저"))
            .andDo(print())
            .andExpect(status().isOk());
    }
}