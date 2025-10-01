package point.ttodoApi.user.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Theme;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.shared.security.AuthorizationService;
import point.ttodoApi.shared.security.CustomPermissionEvaluator;
import point.ttodoApi.user.application.*;
import point.ttodoApi.user.application.command.UpdateUserCommand;
import point.ttodoApi.user.application.query.*;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.presentation.dto.request.UserSearchRequest;
import point.ttodoApi.user.presentation.dto.response.UserResponse;
import point.ttodoApi.user.presentation.mapper.UserPresentationMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({ApiSecurityTestConfig.class, UserControllerTest.MethodSecurityTestConfig.class})
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
    private AuthorizationService authorizationService;

    private static final String BASE_URL = "/user";
    private static final String TEST_USER_USERNAME = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final UUID TEST_USER_ID = UUID.fromString(TEST_USER_USERNAME);

    private User domainUser;
    private Profile profile;
    private UserResult userResult;
    private UserResponse userResponse;
    private UpdateUserCommand updateUserCommand;
    private Page<UserResult> userResultPage;
    private Page<User> userPage;

    @BeforeEach
    void setUp() {
        domainUser = User.builder()
            .id(TEST_USER_ID)
            .email("test@example.com")
            .password("password")
            .build();

        profile = Profile.builder()
            .id(UUID.randomUUID())
            .owner(domainUser)
            .nickname("테스트유저")
            .introduction("자기소개")
            .timeZone("Asia/Seoul")
            .locale("ko-KR")
            .theme(Theme.MODERN)
            .build();

        userResult = new UserResult(TEST_USER_ID, domainUser.getEmail(), profile.getNickname());
        userResponse = new UserResponse(
            userResult.id(),
            userResult.email(),
            userResult.nickname(),
            profile.getIntroduction(),
            profile.getTimeZone(),
            profile.getLocale(),
            profile.getTheme().name(),
            "profile-url"
        );
        updateUserCommand = new UpdateUserCommand(TEST_USER_ID, "Updated User", "소개 수정");

        userResultPage = new PageImpl<>(List.of(userResult));
        userPage = new PageImpl<>(List.of(domainUser));

        given(userQueryService.getUser(any(UserQuery.class))).willReturn(userResult);
        given(profileService.getProfile(any(UUID.class))).willReturn(profile);
        given(userPresentationMapper.toResponse(any(UserResult.class), any(Profile.class))).willReturn(userResponse);
        given(userPresentationMapper.toResponse(any(UserResult.class))).willReturn(userResponse);
        given(userPresentationMapper.toResponse(any(User.class))).willReturn(userResponse);
        given(userQueryService.getuser(any(UserListQuery.class))).willReturn(userResultPage);
        given(userSearchService.searchUsers(any(UserSearchRequest.class), any(Pageable.class))).willReturn(userPage);
        given(userSearchService.getInactiveUsers(anyInt(), any(Pageable.class))).willReturn(userPage);
        given(userPresentationMapper.toCommand(any(UUID.class), any(point.ttodoApi.user.presentation.dto.request.UpdateUserRequest.class)))
            .willReturn(updateUserCommand);
        given(userCommandService.updateUser(any(UpdateUserCommand.class))).willReturn(userResult);
        given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(true);
    }

    @TestConfiguration
    static class MethodSecurityTestConfig {

        @Bean
        CustomPermissionEvaluator customPermissionEvaluator(AuthorizationService authorizationService) {
            return new CustomPermissionEvaluator(authorizationService);
        }

        @Bean
        MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator customPermissionEvaluator) {
            DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
            handler.setPermissionEvaluator(customPermissionEvaluator);
            return handler;
        }
    }

    @Nested
    @DisplayName("1. READ - 사용자 조회")
    class ReadTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("현재 사용자 정보 조회 성공")
            @WithMockUser(username = TEST_USER_USERNAME, roles = "USER")
            void getCurrentUser_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("특정 사용자 정보 조회 성공")
            @WithMockUser(username = TEST_USER_USERNAME, roles = "USER")
            void getUserById_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + TEST_USER_ID))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("관리자 - 사용자 목록 조회 성공")
            @WithMockUser(username = "admin", roles = "ADMIN")
            void getUsers_AsAdmin_Success() throws Exception {
                mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("관리자 - 이메일 키워드로 사용자 검색")
            @WithMockUser(username = "admin", roles = "ADMIN")
            void searchUsers_ByEmail_Success() throws Exception {
                mockMvc.perform(get(BASE_URL)
                        .param("emailKeyword", "@example.com")
                        .param("page", "0"))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("관리자 - 비활성 사용자 조회 성공")
            @WithMockUser(username = "admin", roles = "ADMIN")
            void getInactiveUsers_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/inactive")
                        .param("days", "45"))
                    .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {

            @Test
            @DisplayName("현재 사용자 정보 조회 실패 - 인증 없음")
            void getCurrentUser_Unauthenticated() throws Exception {
                mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("특정 사용자 정보 조회 실패 - 권한 없음")
            @WithMockUser(username = TEST_USER_USERNAME, roles = "USER")
            void getUserById_Forbidden() throws Exception {
                given(authorizationService.hasPermission(eq(TEST_USER_ID), any(), anyString(), anyString())).willReturn(false);

                mockMvc.perform(get(BASE_URL + "/" + TEST_USER_ID))
                    .andExpect(status().isForbidden());

                given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(true);
            }

            @Test
            @DisplayName("사용자 목록 조회 실패 - 관리자 아님")
            @WithMockUser(username = TEST_USER_USERNAME, roles = "USER")
            void getUsers_AsUser_Forbidden() throws Exception {
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("비활성 사용자 조회 실패 - 관리자 아님")
            @WithMockUser(username = TEST_USER_USERNAME, roles = "USER")
            void getInactiveUsers_AsUser_Forbidden() throws Exception {
                mockMvc.perform(get(BASE_URL + "/inactive"))
                    .andExpect(status().isForbidden());
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
            @WithMockUser(username = TEST_USER_USERNAME, roles = "USER")
            void updateUser_Success() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "Updated User")
                        .param("introduction", "소개 수정"))
                    .andExpect(status().isNoContent());

                verify(userCommandService).updateUser(updateUserCommand);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {

            @Test
            @DisplayName("사용자 정보 수정 실패 - 인증 없음")
            void updateUser_Unauthenticated() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "Updated User"))
                    .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("사용자 정보 수정 실패 - 권한 없음")
            @WithMockUser(username = TEST_USER_USERNAME, roles = "USER")
            void updateUser_Forbidden() throws Exception {
                given(authorizationService.hasPermission(eq(TEST_USER_ID), any(), anyString(), anyString())).willReturn(false);

                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "Updated User"))
                    .andExpect(status().isForbidden());

                given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(true);
            }
        }
    }
}
