package point.ttodoApi.challenge.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.challenge.application.*;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.shared.config.auth.SecurityTestConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChallengeLeaderController.class)
@Import({SecurityTestConfig.class})
class ChallengeLeaderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeLeaderService leaderService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private ChallengeService challengeService;

    private static final String BASE_URL = "/challenges/1/leaders";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    @Nested
    @DisplayName("인증 및 권한 테스트")
    class AuthenticationAndAuthorizationTests {

        @Test
        @DisplayName("인증 없이 리더 추가 시도 - 403 반환")
        void addLeader_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("userId", TEST_USER_ID))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 리더 목록 조회 - 403 반환")
        void getLeaders_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 리더 통계 조회 - 403 반환")
        void getLeaderStatistics_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/statistics"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER 권한으로 리더 자진사퇴 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void resignLeader_WithUserRole_Success() throws Exception {
            mockMvc.perform(delete(BASE_URL))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("USER 권한으로 리더 목록 조회 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void getLeaders_WithUserRole_Success() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("USER 권한으로 멤버 역할 조회 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void getUserRole_WithUserRole_Success() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + TEST_USER_ID + "/role"))
                .andExpect(status().isOk());
        }
    }
}