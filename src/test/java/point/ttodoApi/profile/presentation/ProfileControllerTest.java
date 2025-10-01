package point.ttodoApi.profile.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Theme;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.user.application.command.UpdateUserCommand;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("ProfileController 단위 테스트")
@Tag("unit")
@Tag("profile")
@Tag("controller")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private point.ttodoApi.profile.presentation.mapper.ProfilePresentationMapper profilePresentationMapper;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    private static final UUID USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    private Profile createProfile() {
        User owner = User.builder()
            .id(USER_ID)
            .email("user@example.com")
            .password("pw")
            .build();

        return Profile.builder()
            .id(UUID.randomUUID())
            .owner(owner)
            .nickname("테스트유저")
            .introduction("소개")
            .timeZone("Asia/Seoul")
            .locale("ko-KR")
            .theme(Theme.MODERN)
            .build();
    }

    private RequestPostProcessor authenticatedUser(UUID userId) {
        return SecurityMockMvcRequestPostProcessors.user(new TestUserDetails(userId));
    }

    @Nested
    @DisplayName("1. READ - 프로필 조회")
    class ReadTests {

        @Test
        @DisplayName("프로필 조회 성공")
        void getProfile_Success() throws Exception {
            UserResult userResult = new UserResult(USER_ID, "user@example.com", "테스트유저");
            Profile profile = createProfile();
            given(userService.getUser(USER_ID)).willReturn(userResult);
            given(profileService.getProfile(USER_ID)).willReturn(profile);
            given(profilePresentationMapper.toProfileResponse(userResult, profile))
                .willReturn(new point.ttodoApi.profile.presentation.dto.response.ProfileResponse(
                    userResult.nickname(),
                    profile.getIntroduction(),
                    profile.getTimeZone(),
                    profile.getLocale(),
                    profile.getTheme(),
                    profile.getImageUrl()
                ));

            mockMvc.perform(get("/user/{userId}/profile", USER_ID)
                    .with(authenticatedUser(USER_ID)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("프로필 조회 실패 - 다른 사용자")
        void getProfile_Forbidden() throws Exception {
            mockMvc.perform(get("/user/{userId}/profile", USER_ID)
                    .with(authenticatedUser(UUID.randomUUID())))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("프로필 이미지 조회 성공")
        void getProfileImage_Success() throws Exception {
            Profile profile = createProfile();
            profile.setProfileImage(new byte[]{1, 2, 3}, "image/png");
            profile.setImageUrl("https://cdn/image.png");
            given(profileService.getProfile(USER_ID)).willReturn(profile);

            mockMvc.perform(get("/user/{userId}/profile/image", USER_ID)
                    .with(authenticatedUser(USER_ID)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("프로필 이미지 조회 실패 - 없음")
        void getProfileImage_NotFound() throws Exception {
            Profile profile = createProfile();
            given(profileService.getProfile(USER_ID)).willReturn(profile);

            mockMvc.perform(get("/user/{userId}/profile/image", USER_ID)
                    .with(authenticatedUser(USER_ID)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("2. UPDATE - 프로필 수정")
    class UpdateTests {

        @Test
        @DisplayName("프로필 정보 수정 성공")
        void updateProfile_Success() throws Exception {
            Profile profile = createProfile();
            given(profileService.getProfile(USER_ID)).willReturn(profile);
            given(profileService.saveProfile(profile)).willReturn(profile);
            doNothing().when(userService).updateUser(any(UpdateUserCommand.class));

            mockMvc.perform(patch("/user/{userId}/profile", USER_ID)
                    .with(authenticatedUser(USER_ID))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("nickname", "새닉네임")
                    .param("introduction", "새소개")
                    .param("timeZone", "Asia/Tokyo")
                    .param("locale", "ja-JP")
                    .param("theme", Theme.PINE.name()))
                .andExpect(status().isNoContent());

            verify(userService).updateUser(any(UpdateUserCommand.class));
            verify(profileService).saveProfile(profile);
        }

        @Test
        @DisplayName("프로필 이미지 업로드 성공")
        void uploadProfileImage_Success() throws Exception {
            Profile profile = createProfile();
            profile.setImageUrl("https://cdn/new.png");
            given(profileService.updateProfileImage(eq(USER_ID), any())).willReturn(profile);
            given(profilePresentationMapper.toProfileImageUploadResponse(profile.getImageUrl()))
                .willReturn(new point.ttodoApi.profile.presentation.dto.response.ProfileImageUploadResponse(
                    profile.getImageUrl()
                ));

            MockMultipartFile image = new MockMultipartFile(
                "image", "profile.png", "image/png", new byte[]{1, 2, 3}
            );

            mockMvc.perform(multipart("/user/{userId}/profile/image", USER_ID)
                    .file(image)
                    .with(authenticatedUser(USER_ID)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("프로필 이미지 삭제 성공")
        void deleteProfileImage_Success() throws Exception {
            Profile profile = createProfile();
            given(profileService.removeProfileImage(USER_ID)).willReturn(profile);

            mockMvc.perform(delete("/user/{userId}/profile/image", USER_ID)
                    .with(authenticatedUser(USER_ID)))
                .andExpect(status().isNoContent());
        }
    }

    private static class TestUserDetails extends org.springframework.security.core.userdetails.User {
        private final UUID id;

        TestUserDetails(UUID id) {
            super(id.toString(), "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }
}
