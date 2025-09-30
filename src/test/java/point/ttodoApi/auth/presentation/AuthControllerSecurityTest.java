package point.ttodoApi.auth.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.auth.application.AuthCommandService;
import point.ttodoApi.auth.application.AuthQueryService;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.auth.application.command.*;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.auth.presentation.CookieService;
import point.ttodoApi.auth.presentation.dto.request.SignInRequest;
import point.ttodoApi.auth.presentation.dto.request.SignUpRequest;
import point.ttodoApi.auth.presentation.mapper.AuthPresentationMapper;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.shared.error.ErrorCode;
import point.ttodoApi.shared.validation.sanitizer.ValidationUtils;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 단위 테스트 - 보안 및 기본 기능 검증
 * ApiSecurityTestConfig를 사용하여 모든 요청 허용 + @WithMockUser 패턴 적용
 */
@WebMvcTest(AuthController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("AuthController MockMvc 테스트")
@Tag("unit")
@Tag("auth")
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private AuthCommandService authCommandService;
    
    @MockitoBean
    private AuthQueryService authQueryService;
    
    @MockitoBean
    private TokenService tokenService;
    
    @MockitoBean
    private AuthPresentationMapper authMapper;
    
    @MockitoBean
    private CookieService cookieService;
    
    @MockitoBean
    private ValidationUtils validationUtils;

    private static final String BASE_URL = "/auth";
    private static final UUID TEST_USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    
    @BeforeEach
    void setUp() {
        // Mock validation utils to always pass validation
        given(validationUtils.sanitizeHtmlStrict(any(String.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(validationUtils.sanitizeHtml(any(String.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(validationUtils.isValidEmail(any(String.class))).willReturn(true);
        given(validationUtils.containsSqlInjectionPattern(any(String.class))).willReturn(false);
        given(validationUtils.isValidPassword(any(String.class))).willReturn(true);
        given(validationUtils.isValidUsername(any(String.class))).willReturn(true);
        
        // Mock mappers
        given(authMapper.toCommand(any(SignUpRequest.class), any(String.class), any(String.class)))
            .willAnswer(invocation -> {
                SignUpRequest request = invocation.getArgument(0);
                String nickname = invocation.getArgument(1);
                return new SignUpCommand(
                    request.email(),
                    request.password(),
                    nickname,
                    null,
                    "default-device-id"
                );
            });
            
        given(authMapper.toCommand(any(SignInRequest.class), any(String.class)))
            .willAnswer(invocation -> {
                SignInRequest request = invocation.getArgument(0);
                String deviceId = invocation.getArgument(1);
                return new SignInCommand(
                    request.email(),
                    request.password(),
                    deviceId != null ? deviceId : "default-device-id"
                );
            });
            
        given(authMapper.toSignOutCommand(any(), any()))
            .willReturn(new SignOutCommand("test-device", "test-token"));
        
        // Mock service void methods
        org.mockito.Mockito.doNothing().when(authCommandService).signOut(any());
        org.mockito.Mockito.doNothing().when(cookieService).setJwtCookie(any(), any());
        org.mockito.Mockito.doNothing().when(cookieService).setRefreshCookie(any(), any());
        org.mockito.Mockito.doNothing().when(cookieService).setExpiredJwtCookie(any());
        org.mockito.Mockito.doNothing().when(cookieService).setExpiredRefreshCookie(any());
    }

    @Test
    @DisplayName("Security test - all auth endpoints accessible (permits all)")
    void authEndpoints_SecurityPermitsAll() throws Exception {
        // Verify that ApiSecurityTestConfig permits all requests (no 403 Forbidden)
        
        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "test123"))
            .andExpect(status().is(not(403))); // Security permits access
            
        mockMvc.perform(post(BASE_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  
                .param("email", "test@example.com")
                .param("password", "test123"))
            .andExpect(status().is(not(403))); // Security permits access
            
        mockMvc.perform(post(BASE_URL + "/sign-out"))
            .andExpect(status().is(not(403))); // Security permits access
            
        mockMvc.perform(post(BASE_URL + "/refresh"))
            .andExpect(status().is(not(403))); // Security permits access
    }
    
    @Test
    @DisplayName("@WithMockUser 테스트 - 인증된 사용자 접근")
    @WithMockUser(username = "ffffffff-ffff-ffff-ffff-ffffffffffff")
    void withMockUser_AuthenticatedAccess() throws Exception {
        // Verify @WithMockUser works with ApiSecurityTestConfig
        mockMvc.perform(post(BASE_URL + "/sign-out"))
            .andExpect(status().isOk()); // Should work with proper mocking
    }
    
    @Test
    @DisplayName("회원가입 성공 - 완전한 Mock 설정")
    void signUp_Success_WithCompleteMocking() throws Exception {
        // Given - Mock successful signup
        AuthResult mockResult = new AuthResult(
            "mock-access-token",
            "mock-refresh-token",
            "default-device",
            TEST_USER_ID,
            "test@example.com",
            "테스트유저"
        );
        
        given(authCommandService.signUp(any(SignUpCommand.class))).willReturn(mockResult);
        
        // When & Then
        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!")
                .param("nickname", "테스트유저"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("로그인 성공 - 완전한 Mock 설정")  
    void signIn_Success_WithCompleteMocking() throws Exception {
        // Given - Mock successful signin
        AuthResult mockResult = new AuthResult(
            "mock-access-token",
            "mock-refresh-token", 
            "test-device",
            TEST_USER_ID,
            "test@example.com",
            "테스트유저"
        );
        
        given(authCommandService.signIn(any(SignInCommand.class))).willReturn(mockResult);
        
        // When & Then
        mockMvc.perform(post(BASE_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com") 
                .param("password", "Password123!")
                .param("deviceId", "test-device"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("비즈니스 예외 처리 - 중복 이메일")
    void signUp_BusinessException_DuplicateEmail() throws Exception {
        // Given - Mock business exception
        given(authCommandService.signUp(any(SignUpCommand.class)))
            .willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));
        
        // When & Then - Should return proper error response
        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "duplicate@example.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!")
                .param("nickname", "테스트유저"))
            .andExpect(status().isConflict()); // GlobalExceptionHandler should handle this
    }
}