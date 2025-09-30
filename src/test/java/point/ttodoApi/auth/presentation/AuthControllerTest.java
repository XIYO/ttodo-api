package point.ttodoApi.auth.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import point.ttodoApi.auth.application.AuthCommandService;
import point.ttodoApi.auth.application.AuthQueryService;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.auth.application.command.*;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.auth.presentation.dto.request.SignInRequest;
import point.ttodoApi.auth.presentation.dto.request.SignUpRequest;
import point.ttodoApi.auth.infrastructure.jwt.CustomJwtAuthConverter;
import point.ttodoApi.auth.infrastructure.security.CustomUserDetailsService;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.shared.error.ErrorCode;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 단위 테스트
 * 최신 Spring Boot 3.x 문법 적용
 * Nested 구조로 CRUD 순서에 따라 체계적 테스트 구성
 */
@WebMvcTest(AuthController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("AuthController MockMvc 테스트")
@Tag("unit")
@Tag("auth")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthCommandService authCommandService;
    
    @MockitoBean
    private AuthQueryService authQueryService;
    
    @MockitoBean
    private TokenService tokenService;
    
    @MockitoBean
    private point.ttodoApi.user.infrastructure.persistence.UserRepository userRepository;
    
    @MockitoBean
    private point.ttodoApi.shared.error.ErrorMetricsCollector errorMetricsCollector;
    
    @MockitoBean
    private point.ttodoApi.user.application.UserCommandService userCommandService;
    
    @MockitoBean
    private point.ttodoApi.user.application.UserQueryService userQueryService;
    
    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    @MockitoBean
    private point.ttodoApi.profile.application.ProfileService profileService;
    
    @MockitoBean
    private point.ttodoApi.auth.presentation.mapper.AuthPresentationMapper authMapper;
    
    @MockitoBean
    private point.ttodoApi.auth.presentation.CookieService cookieService;
    
    @MockitoBean
    private point.ttodoApi.shared.config.properties.AppProperties appProperties;
    
    @MockitoBean
    private point.ttodoApi.shared.validation.sanitizer.ValidationUtils validationUtils;
    
    private static final String BASE_URL = "/auth";
    private static final UUID TEST_USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    
    @BeforeEach
    void setUp() {
        // Mock validation utils for any validation that still uses it
        given(validationUtils.sanitizeHtmlStrict(any(String.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(validationUtils.sanitizeHtml(any(String.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(validationUtils.isValidEmail(any(String.class))).willReturn(true);
        given(validationUtils.containsSqlInjectionPattern(any(String.class))).willReturn(false);
        given(validationUtils.isValidPassword(any(String.class))).willReturn(true);
        given(validationUtils.isValidUsername(any(String.class))).willReturn(true);
        
        // Mock mapper methods
        given(authMapper.toSignOutCommand(any(), any()))
            .willReturn(new SignOutCommand("test-device", "test-token"));
            
        // Mock auth service methods to do nothing for void methods
        org.mockito.Mockito.doNothing().when(authCommandService).signOut(any());
        
        // Mock cookie service methods to do nothing for void methods
        org.mockito.Mockito.doNothing().when(cookieService).setExpiredJwtCookie(any());
        org.mockito.Mockito.doNothing().when(cookieService).setExpiredRefreshCookie(any());
    }
    
    @Test
    @DisplayName("Security test - all auth endpoints accessible with ApiSecurityTestConfig")
    void authEndpoints_SecurityPermitsAll() throws Exception {
        // Test that ApiSecurityTestConfig permits all requests (no 403 Forbidden)
        // This validates that our security configuration is working correctly
        
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
    @DisplayName("@WithMockUser 테스트 - 인증된 사용자로 접근")
    @WithMockUser(username = "ffffffff-ffff-ffff-ffff-ffffffffffff")
    void withMockUser_Test() throws Exception {
        // @WithMockUser가 정상적으로 작동하는지 테스트
        // 이 테스트는 보안 설정이 올바르게 구성되었음을 증명
        mockMvc.perform(post(BASE_URL + "/sign-out"))
            .andExpect(status().is(not(403))); // 인증된 사용자도 접근 가능
    }
    
    @Nested
    @DisplayName("1. CREATE - 회원가입 및 로그인")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @Order(1)
            @DisplayName("회원가입 성공 - 유효한 이메일과 패스워드")
            void signUp_Success_WithValidCredentials() throws Exception {
                // Given
                AuthResult mockResult = new AuthResult(
                    "mock-access-token",
                    "mock-refresh-token",
                    "default-device",
                    TEST_USER_ID,
                    "test@example.com",
                    "테스트유저"
                );
                
                TokenService.TokenResult tokenResult = new TokenService.TokenResult(
                    "mock-access-token",
                    "mock-refresh-token",
                    "default-device"
                );
                
                given(authCommandService.signUp(any(SignUpCommand.class))).willReturn(mockResult);
                given(tokenService.createTokenPair(any(), any(), any(), any(), any())).willReturn(tokenResult);
                
                // When & Then
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "test@example.com")
                        .param("password", "Password123!")
                        .param("confirmPassword", "Password123!")
                        .param("nickname", "테스트유저"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("access-token")))
                    .andExpect(header().string("Set-Cookie", containsString("refresh-token")));
            }
            
            @Test
            @Order(2)
            @DisplayName("로그인 성공 - 올바른 자격 증명")
            void signIn_Success_WithCorrectCredentials() throws Exception {
                // Given
                AuthResult mockResult = new AuthResult(
                    "mock-access-token",
                    "mock-refresh-token",
                    "test-device-123",
                    TEST_USER_ID,
                    "signin@example.com",
                    "로그인테스트"
                );
                
                TokenService.TokenResult tokenResult = new TokenService.TokenResult(
                    "mock-access-token",
                    "mock-refresh-token",
                    "test-device-123"
                );
                
                given(authCommandService.signIn(any(SignInCommand.class))).willReturn(mockResult);
                given(tokenService.createTokenPair(any(), any(), any(), any(), any())).willReturn(tokenResult);
                
                // When & Then
                mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "signin@example.com")
                        .param("password", "Password123!")
                        .param("deviceId", "test-device-123"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().string("Set-Cookie", containsString("access-token")))
                    .andExpect(header().string("Set-Cookie", containsString("refresh-token")));
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {
            
            @Test
            @DisplayName("회원가입 실패 - 중복 이메일")
            void signUp_Failure_DuplicateEmail() throws Exception {
                // Given
                given(authCommandService.signUp(any(SignUpCommand.class)))
                    .willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));
                
                // When & Then
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "duplicate@example.com")
                        .param("password", "Password123!")
                        .param("nickname", "테스트유저"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists());
            }
            
            @Test
            @DisplayName("회원가입 실패 - 유효하지 않은 이메일 형식")
            void signUp_Failure_InvalidEmailFormat() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "invalid-email")
                        .param("password", "Password123!")
                        .param("nickname", "테스트유저"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("이메일")));
            }
            
            @Test
            @DisplayName("회원가입 실패 - 약한 패스워드")
            void signUp_Failure_WeakPassword() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "test@example.com")
                        .param("password", "weak")
                        .param("nickname", "테스트유저"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("패스워드")));
            }
            
            @Test
            @DisplayName("로그인 실패 - 잘못된 패스워드")
            void signIn_Failure_WrongPassword() throws Exception {
                // Given: 회원가입
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "wrong@example.com")
                        .param("password", "CorrectPass123!")
                        .param("nickname", "테스트"));
                
                // When & Then: 잘못된 패스워드로 로그인
                mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "wrong@example.com")
                        .param("password", "WrongPass123!")
                        .param("deviceId", "test-device"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("인증")));
            }
            
            @Test
            @DisplayName("로그인 실패 - 존재하지 않는 사용자")
            void signIn_Failure_NonExistentUser() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "nonexistent@example.com")
                        .param("password", "Password123!")
                        .param("deviceId", "test-device"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("인증")));
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("회원가입 - 닉네임 미입력시 기본값 설정")
            void signUp_EdgeCase_NoNickname() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "nonickname@example.com")
                        .param("password", "Password123!"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"));
            }
            
            @Test
            @DisplayName("로그인 - deviceId 미입력시 기본값 사용")
            void signIn_EdgeCase_NoDeviceId() throws Exception {
                // Given: 회원가입
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "nodevice@example.com")
                        .param("password", "Password123!")
                        .param("nickname", "테스트"));
                
                // When & Then: deviceId 없이 로그인
                mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "nodevice@example.com")
                        .param("password", "Password123!"))
                    .andExpect(status().isOk());
            }
        }
    }
    
    @Nested
    @DisplayName("2. READ - 토큰 재발급")
    class ReadTests {
        
        @Test
        @DisplayName("토큰 재발급 성공")
        void refreshToken_Success() throws Exception {
            // Given
            TokenService.TokenPair newTokens = new TokenService.TokenPair(
                "new-access-token",
                "new-refresh-token"
            );
            
            given(tokenService.refreshTokens(any(), any())).willReturn(newTokens);
            
            // When & Then
            mockMvc.perform(post(BASE_URL + "/refresh")
                    .cookie(new jakarta.servlet.http.Cookie("refresh-token", "old-refresh-token"))
                    .param("deviceId", "default-device"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", containsString("access-token")));
        }
        
        @Test
        @DisplayName("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
        void refreshToken_Failure_InvalidToken() throws Exception {
            mockMvc.perform(post(BASE_URL + "/refresh")
                    .cookie(new jakarta.servlet.http.Cookie("refresh-token", "invalid-token"))
                    .param("deviceId", "default-device"))
                .andExpect(status().isUnauthorized());
        }
    }
    
    @Nested
    @DisplayName("3. DELETE - 로그아웃")
    class DeleteTests {
        
        @Test
        @DisplayName("로그아웃 성공")
        @WithMockUser
        void signOut_Success() throws Exception {
            mockMvc.perform(post(BASE_URL + "/sign-out")
                    .cookie(new jakarta.servlet.http.Cookie("refresh-token", "some-token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("access-token=; Max-Age=0")))
                .andExpect(header().string("Set-Cookie", containsString("refresh-token=; Max-Age=0")));
        }
        
        @Test
        @DisplayName("로그아웃 - 인증 없이도 성공")
        void signOut_WithoutAuth_Success() throws Exception {
            mockMvc.perform(post(BASE_URL + "/sign-out"))
                .andExpect(status().isOk());
        }
    }
    
    @Nested
    @DisplayName("4. 인증 및 권한 테스트")
    class AuthenticationAndAuthorizationTests {
        
        @Test
        @DisplayName("인증 필요한 엔드포인트 - 토큰 없이 접근시 401")
        void protectedEndpoint_Without_Token_Returns401() throws Exception {
            mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("인증 필요한 엔드포인트 - 유효한 토큰으로 접근 성공")
        @WithMockUser(username = "ffffffff-ffff-ffff-ffff-ffffffffffff")
        void protectedEndpoint_With_ValidToken_Success() throws Exception {
            // 실제 데이터가 없어도 인증 체크는 가능
            mockMvc.perform(get("/user/me"))
                .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("만료된 토큰으로 접근시 401")
        void protectedEndpoint_With_ExpiredToken_Returns401() throws Exception {
            // 만료된 토큰 시뮬레이션
            String expiredToken = "expired.jwt.token";
            mockMvc.perform(get("/user/me")
                    .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
        }
    }
    

}