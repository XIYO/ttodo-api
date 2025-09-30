package point.ttodoApi.auth.presentation;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.auth.application.*;
import point.ttodoApi.auth.application.command.*;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.auth.presentation.dto.request.*;
import point.ttodoApi.auth.presentation.mapper.AuthPresentationMapper;
import point.ttodoApi.common.fixture.AuthFixtures;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.*;
import point.ttodoApi.user.application.*;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;


@DisplayName("AuthController 단위 테스트")
@Tag("unit")
@Tag("auth")
@Tag("controller")
@WebMvcTest(AuthController.class)
@Import(ApiSecurityTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private AuthCommandService authCommandService;
    
    @MockitoBean
    private AuthQueryService authQueryService;
    
    @MockitoBean
    private UserCommandService userCommandService;
    
    @MockitoBean
    private UserQueryService userQueryService;
    
    @MockitoBean
    private ProfileService profileService;
    
    @MockitoBean
    private AuthPresentationMapper authMapper;
    
    @MockitoBean
    private CookieService cookieService;
    
    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;
    
    @MockitoBean
    private UserRepository userRepository;
    
    @MockitoBean 
    private ProfileRepository profileRepository;

    private static final String BASE_URL = "/auth";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "Password123!";
    private static final String NICKNAME = "테스트유저";

    @BeforeEach
    void setUp() {
        // Mock repository behaviors
            given(userRepository.existsByEmail(org.mockito.Mockito.anyString())).willReturn(false);
        
        // Mock mapper commands
            given(authMapper.toSignOutCommand(org.mockito.Mockito.any(), org.mockito.Mockito.any()))
            .willReturn(new SignOutCommand("test-device", "test-token"));
            given(authMapper.toCommand(org.mockito.Mockito.any(SignUpRequest.class), org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString()))
            .willAnswer(invocation -> new SignUpCommand(
                "test@example.com",
                "Password123!",
                "테스트유저",
                null,
                "default-device-id"
            ));
            given(authMapper.toCommand(org.mockito.Mockito.any(SignInRequest.class), org.mockito.Mockito.anyString()))
            .willAnswer(invocation -> new SignInCommand(
                "signin@example.com",
                "Password123!",
                "test-device-123"
            ));
            
        // Mock service methods
            org.mockito.Mockito.doNothing().when(authCommandService).signOut(org.mockito.Mockito.any());
            org.mockito.Mockito.doNothing().when(cookieService).setJwtCookie(org.mockito.Mockito.any(), org.mockito.Mockito.anyString());
            org.mockito.Mockito.doNothing().when(cookieService).setRefreshCookie(org.mockito.Mockito.any(), org.mockito.Mockito.anyString());
            org.mockito.Mockito.doNothing().when(cookieService).setExpiredJwtCookie(org.mockito.Mockito.any());
            org.mockito.Mockito.doNothing().when(cookieService).setExpiredRefreshCookie(org.mockito.Mockito.any());
        
        // 기본 성공 응답 설정
        AuthResult defaultAuthResult = new AuthResult(
            "mock-access-token",
            "mock-refresh-token",
            "default-device",
            java.util.UUID.fromString(TEST_USER_ID),
            "test@example.com",
            "테스트유저"
        );
            given(authCommandService.signUp(org.mockito.Mockito.any(SignUpCommand.class))).willReturn(defaultAuthResult);
            given(authCommandService.signIn(org.mockito.Mockito.any(SignInCommand.class))).willReturn(defaultAuthResult);
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
                AuthResult result = AuthFixtures.createAuthResult();
                     given(authCommandService.signUp(org.mockito.Mockito.any(SignUpCommand.class)))
                    .willReturn(result);
                
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
            @Order(2)
            @DisplayName("로그인 성공 - 올바른 자격 증명")
            void signIn_Success_WithCorrectCredentials() throws Exception {
                // Given
                AuthResult result = AuthFixtures.createAuthResult();
                     given(authCommandService.signIn(org.mockito.Mockito.any(SignInCommand.class)))
                    .willReturn(result);
                
                // When & Then
                mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "signin@example.com")
                        .param("password", "Password123!")
                        .param("deviceId", "test-device-123"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("회원가입 실패 - 중복 이메일")
            void signUp_Failure_DuplicateEmail() throws Exception {
                     given(authCommandService.signUp(org.mockito.Mockito.any(SignUpCommand.class)))
                    .willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));
                
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "duplicate@example.com")
                        .param("password", "Password123!")
                        .param("confirmPassword", "Password123!")
                        .param("nickname", "테스트유저"))
                    .andExpect(status().isConflict());
            }
            
            @Test
            @DisplayName("로그인 실패 - 잘못된 패스워드")
            void signIn_Failure_WrongPassword() throws Exception {
                     given(authCommandService.signIn(org.mockito.Mockito.any(SignInCommand.class)))
                    .willThrow(new BusinessException(ErrorCode.AUTHENTICATION_FAILED));
                
                mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "wrong@example.com")
                        .param("password", "WrongPass123!")
                        .param("deviceId", "test-device"))
                    .andExpect(status().isUnauthorized());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("회원가입 실패 - 유효하지 않은 이메일 형식")
            void signUp_Failure_InvalidEmailFormat() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "invalid-email")
                        .param("password", "Password123!")
                        .param("confirmPassword", "Password123!")
                        .param("nickname", "테스트유저"))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("회원가입 실패 - 약한 패스워드")
            void signUp_Failure_WeakPassword() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "test@example.com")
                        .param("password", "weak")
                        .param("confirmPassword", "weak")
                        .param("nickname", "테스트유저"))
                    .andExpect(status().isBadRequest());
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("회원가입 실패 - 닉네임 미입력")
            void signUp_EdgeCase_NoNickname() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "nonickname@example.com")
                        .param("password", "Password123!")
                        .param("confirmPassword", "Password123!"))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("로그인 - deviceId 미입력시 기본값 사용")
            void signIn_EdgeCase_NoDeviceId() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "nodevice@example.com")
                        .param("password", "Password123!"))
                    .andExpect(status().isOk());
            }
        }
    }
    
    @Nested
    @DisplayName("2. DELETE - 로그아웃")
    class DeleteTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("로그아웃 성공")
            @WithMockUser(username = TEST_USER_ID)
            void signOut_Success() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-out")
                        .cookie(new jakarta.servlet.http.Cookie("refresh-token", "some-token")))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("로그아웃 - 인증 없이도 성공")
            void signOut_WithoutAuth_Success() throws Exception {
                mockMvc.perform(post(BASE_URL + "/sign-out"))
                    .andExpect(status().isOk());
            }
        }
    }
    
    @Nested
    @DisplayName("0. SECURITY - 접근 허용 패턴")
    class SecurityPattern {
        @Test
        @DisplayName("Security Config - 모든 엔드포인트 403 아님")
        void permitsAllAuthEndpoints() throws Exception {
            mockMvc.perform(post(BASE_URL + "/sign-up")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("email", EMAIL)
                    .param("password", PASSWORD))
                .andExpect(status().is(not(403)));

            mockMvc.perform(post(BASE_URL + "/sign-in")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("email", EMAIL)
                    .param("password", PASSWORD))
                .andExpect(status().is(not(403)));

            mockMvc.perform(post(BASE_URL + "/sign-out"))
                .andExpect(status().is(not(403)));
        }
    }

    @Nested
    @DisplayName("JSON 변형 케이스")
    class JsonVariant {
        @Test
        @DisplayName("회원가입 - JSON 본문")
        void signUp_WithJsonBody() throws Exception {
            AuthResult result = AuthFixtures.createAuthResult();
                given(authCommandService.signUp(org.mockito.Mockito.any(SignUpCommand.class))).willReturn(result);
                given(authMapper.toCommand(org.mockito.Mockito.any(SignUpRequest.class), org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString()))
                .willReturn(new SignUpCommand(EMAIL, PASSWORD, NICKNAME, null, "device"));
            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                java.util.Map.of(
                    "email", EMAIL,
                    "password", PASSWORD,
                    "confirmPassword", PASSWORD,
                    "nickname", NICKNAME,
                    "introduction", "",
                    "timeZone", "Asia/Seoul",
                    "locale", "ko_KR"
                )
            );
            mockMvc.perform(post(BASE_URL + "/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                .andExpect(status().isOk());
        }
    }

}