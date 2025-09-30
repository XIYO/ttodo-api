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
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.shared.validation.service.DisposableEmailService;
import point.ttodoApi.shared.validation.service.ForbiddenWordService;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private ErrorMetricsCollector errorMetricsCollector;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DisposableEmailService disposableEmailService;

    @MockitoBean
    private ForbiddenWordService forbiddenWordService;

    private static final UUID TEST_USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_NICKNAME = "테스트유저";
    private static final String TEST_DEVICE_ID = "test-device";
    private static final String DUPLICATE_EMAIL = "duplicate@example.com";
    
    @BeforeEach
    void setUp() {
        given(authMapper.toCommand(any(SignUpRequest.class), any(String.class), any(String.class)))
            .willAnswer(invocation -> {
                SignUpRequest request = invocation.getArgument(0);
                String nickname = invocation.getArgument(1);
                String introduction = invocation.getArgument(2);
                return new SignUpCommand(
                    request.email(),
                    request.password(),
                    nickname,
                    introduction,
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

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(disposableEmailService.isDisposableEmail(anyString())).willReturn(false);
        given(forbiddenWordService.containsForbiddenWord(anyString())).willReturn(false);
        
        AuthResult defaultAuthResult = new AuthResult(
            "mock-access-token",
            "mock-refresh-token",
            "default-device",
            TEST_USER_ID,
            TEST_EMAIL,
            TEST_NICKNAME
        );
        given(authCommandService.signUp(any())).willReturn(defaultAuthResult);
        given(authCommandService.signIn(any())).willReturn(defaultAuthResult);
        
        org.mockito.Mockito.doNothing().when(authCommandService).signOut(any());
        org.mockito.Mockito.doNothing().when(cookieService).setJwtCookie(any(), any());
        org.mockito.Mockito.doNothing().when(cookieService).setRefreshCookie(any(), any());
        org.mockito.Mockito.doNothing().when(cookieService).setExpiredJwtCookie(any());
        org.mockito.Mockito.doNothing().when(cookieService).setExpiredRefreshCookie(any());
    }

    @Test
    @DisplayName("Security test - all auth endpoints accessible (permits all)")
    void authEndpoints_SecurityPermitsAll() throws Exception {        
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", TEST_EMAIL)
                .param("password", TEST_PASSWORD))
            .andExpect(status().is(not(403)));
            
        mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  
                .param("email", TEST_EMAIL)
                .param("password", TEST_PASSWORD))
            .andExpect(status().is(not(403)));
            
        mockMvc.perform(post("/auth/sign-out"))
            .andExpect(status().is(not(403)));
            
        mockMvc.perform(post("/auth/refresh"))
            .andExpect(status().is(not(403)));
    }
    
    @Test
    @DisplayName("@WithMockUser 테스트 - 인증된 사용자 접근")
    @WithMockUser
    void withMockUser_AuthenticatedAccess() throws Exception {
        mockMvc.perform(post("/auth/sign-out")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("회원가입 성공 - 완전한 Mock 설정")
    void signUp_Success_WithCompleteMocking() throws Exception {
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", TEST_EMAIL)
                .param("password", TEST_PASSWORD)
                .param("confirmPassword", TEST_PASSWORD)
                .param("nickname", TEST_NICKNAME))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("로그인 성공 - 완전한 Mock 설정")  
    void signIn_Success_WithCompleteMocking() throws Exception {
        mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", TEST_EMAIL) 
                .param("password", TEST_PASSWORD)
                .param("deviceId", TEST_DEVICE_ID))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("비즈니스 예외 처리 - 중복 이메일")
    void signUp_BusinessException_DuplicateEmail() throws Exception {
        given(userRepository.existsByEmail(DUPLICATE_EMAIL)).willReturn(true);
        
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", DUPLICATE_EMAIL)
                .param("password", TEST_PASSWORD)
                .param("confirmPassword", TEST_PASSWORD)
                .param("nickname", TEST_NICKNAME))
            .andExpect(status().isBadRequest());
    }
}
