package point.ttodoApi.user.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.user.application.*;
import point.ttodoApi.user.presentation.mapper.UserPresentationMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 단위 테스트
 * 개별 Mock 방식 적용
 * @WithMockUser 기반 인증 처리
 * CRUD 순서 + Nested 구조 + 한글 DisplayName
 */
@WebMvcTest(UserController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("UserController 단위 테스트")
@Tag("unit")
@Tag("user")
@Tag("controller")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private UserSearchService userSearchService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private UserPresentationMapper userPresentationMapper;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    @MockitoBean
    private point.ttodoApi.user.infrastructure.persistence.UserRepository userRepository;

    @MockitoBean
    private point.ttodoApi.profile.infrastructure.persistence.ProfileRepository profileRepository;

    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    @BeforeEach
    void setUp() {
        // Mock User for Profile
        point.ttodoApi.user.domain.User mockUser = point.ttodoApi.user.domain.User.builder()
            .id(java.util.UUID.fromString(TEST_USER_ID))
            .email("test@example.com")
            .password("password")
            .build();

        // Mock UserRepository
        given(userRepository.findById(any())).willReturn(java.util.Optional.of(mockUser));
        given(userRepository.save(any())).willReturn(mockUser);

        // Mock UserResult
        point.ttodoApi.user.application.result.UserResult userResult =
            new point.ttodoApi.user.application.result.UserResult(
                java.util.UUID.fromString(TEST_USER_ID),
                "test@example.com",
                "Test User"
            );

        // Mock Profile
        point.ttodoApi.profile.domain.Profile profile = point.ttodoApi.profile.domain.Profile.builder()
            .id(java.util.UUID.fromString(TEST_USER_ID))
            .owner(mockUser)
            .nickname("Test User")
            .theme(point.ttodoApi.profile.domain.Theme.PINKY)
            .introduction("Test Introduction")
            .timeZone("Asia/Seoul")
            .locale("ko_KR")
            .build();

        // Mock UserResponse
        point.ttodoApi.user.presentation.dto.response.UserResponse userResponse =
            new point.ttodoApi.user.presentation.dto.response.UserResponse(
                java.util.UUID.fromString(TEST_USER_ID),
                "test@example.com",
                "Test User",
                "Test Introduction",
                "Asia/Seoul",
                "ko_KR",
                "PINKY",
                null
            );

        // Mock UpdateUserCommand
        point.ttodoApi.user.application.command.UpdateUserCommand updateCommand =
            new point.ttodoApi.user.application.command.UpdateUserCommand(
                java.util.UUID.fromString(TEST_USER_ID),
                "Updated User",
                "Updated Introduction"
            );

        // Repository mocks
        given(profileRepository.findById(any())).willReturn(java.util.Optional.of(profile));
        given(profileRepository.save(any())).willReturn(profile);

        // Service mocks
        given(userQueryService.getUser(any())).willReturn(userResult);
        given(profileService.getProfile(any())).willReturn(profile);
        given(profileService.saveProfile(any())).willReturn(profile);
        given(userCommandService.updateUser(any())).willReturn(userResult);

        // Mapper mocks
        given(userPresentationMapper.toResponse(any(point.ttodoApi.user.application.result.UserResult.class), any()))
            .willReturn(userResponse);
        given(userPresentationMapper.toCommand(any(), any()))
            .willReturn(updateCommand);
    }

    @Nested
    @DisplayName("1. READ - 사용자 조회")
    class ReadTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("사용자 정보 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getUser_Success() throws Exception {
                mockMvc.perform(get("/user/me"))
                    .andExpect(status().isOk());
            }
        }
    }

    @Nested
    @DisplayName("2. UPDATE - 사용자 수정")
    class UpdateTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("사용자 정보 수정 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void updateUser_Success() throws Exception {
                mockMvc.perform(patch("/user/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "Updated User"))
                    .andExpect(status().isNoContent());
            }
        }
    }

}