package point.ttodoApi.todo.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.shared.config.auth.SecurityTestConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PriorityController.class)
@Import({SecurityTestConfig.class})
class PriorityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/priorities";

    @Nested
    @DisplayName("인증 및 권한 테스트")
    class AuthenticationAndAuthorizationTests {

        @Test
        @DisplayName("인증 없이 접근 - 403 반환")
        void withoutAuth_Returns403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER 권한으로 접근 성공")
        @WithMockUser(username = "ffffffff-ffff-ffff-ffff-ffffffffffff", roles = "USER")
        void withUserRole_Success() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].name").exists());
        }

        @Test
        @DisplayName("ADMIN 권한으로 접근 성공")
        @WithMockUser(username = "ffffffff-ffff-ffff-ffff-ffffffffffff", roles = {"USER", "ADMIN"})
        void withAdminRole_Success() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        }
    }
}