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
import point.ttodoApi.challenge.presentation.mapper.ChallengePresentationMapper;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.shared.config.auth.SecurityTestConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChallengeController.class)
@Import({SecurityTestConfig.class})
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeService challengeService;

    @MockitoBean
    private ChallengeSearchService challengeSearchService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ChallengePresentationMapper challengePresentationMapper;

    private static final String BASE_URL = "/challenges";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    @Nested
    @DisplayName("인증 및 권한 테스트")
    class AuthenticationAndAuthorizationTests {

        @Test
        @DisplayName("인증 없이 챌린지 목록 조회 - 403 반환")
        void getChallenges_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 챌린지 상세 조회 - 403 반환")
        void getChallenge_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 챌린지 생성 시도 - 403 반환")
        void createChallenge_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "Test Challenge")
                    .param("description", "Test Description")
                    .param("startDate", "2025-01-01")
                    .param("endDate", "2025-01-31"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER 권한으로 챌린지 목록 조회 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void getChallenges_WithUserRole_Success() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("USER 권한으로 챌린지 상세 조회 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void getChallenge_WithUserRole_Success() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("USER 권한으로 챌린지 생성 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void createChallenge_WithUserRole_Success() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "Test Challenge")
                    .param("description", "Test Description")
                    .param("startDate", "2025-01-01")
                    .param("endDate", "2025-01-31"))
                .andExpect(status().isCreated());
        }
    }
}