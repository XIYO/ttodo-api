package point.ttodoApi.category.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.category.application.*;
import point.ttodoApi.category.presentation.mapper.CategoryCollaboratorPresentationMapper;
import point.ttodoApi.shared.config.auth.SecurityTestConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryCollaboratorController.class)
@Import({SecurityTestConfig.class})
class CategoryCollaboratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryCollaboratorService collaboratorService;

    @MockitoBean
    private CategoryCollaboratorPresentationMapper collaboratorMapper;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    private static final String BASE_URL = "/categories/550e8400-e29b-41d4-a716-446655440000/collaborators";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    @Nested
    @DisplayName("인증 및 권한 테스트")
    class AuthenticationAndAuthorizationTests {

        @Test
        @DisplayName("인증 없이 협업자 초대 시도 - 403 반환")
        void inviteCollaborator_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("userId", TEST_USER_ID))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 협업자 목록 조회 - 403 반환")
        void getCollaborators_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 초대 수락 시도 - 403 반환")
        void acceptInvitation_WithoutAuth_Returns403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/accept")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER 권한으로 초대 수락 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void acceptInvitation_WithUserRole_Success() throws Exception {
            mockMvc.perform(post(BASE_URL + "/accept")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("USER 권한으로 초대 거절 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void rejectInvitation_WithUserRole_Success() throws Exception {
            mockMvc.perform(post(BASE_URL + "/reject")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("USER 권한으로 협업 나가기 성공")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void leaveCollaboration_WithUserRole_Success() throws Exception {
            mockMvc.perform(delete(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNoContent());
        }
    }
}