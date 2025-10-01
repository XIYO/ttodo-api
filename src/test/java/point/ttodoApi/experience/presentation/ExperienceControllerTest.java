package point.ttodoApi.experience.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.experience.application.ExperienceService;
import point.ttodoApi.experience.application.result.UserLevelResult;
import point.ttodoApi.experience.presentation.dto.response.UserLevelResponse;
import point.ttodoApi.experience.presentation.mapper.ExperiencePresentationMapper;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.ErrorMetricsCollector;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExperienceController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("ExperienceController 단위 테스트")
@Tag("unit")
@Tag("experience")
@Tag("controller")
class ExperienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExperienceService experienceService;

    @MockitoBean
    private ExperiencePresentationMapper experiencePresentationMapper;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    @Test
    @DisplayName("회원 경험치 조회 성공")
    @WithMockUser(roles = "USER")
    void getUserLevel_Success() throws Exception {
        UserLevelResult result = new UserLevelResult(5, "골드", 850, 700, 150, 150, 300);
        UserLevelResponse response = new UserLevelResponse(5, "골드", 850, 700, 150, 150, 300);
        given(experienceService.getUserLevel(any(UUID.class))).willReturn(result);
        given(experiencePresentationMapper.toResponse(result)).willReturn(response);

        mockMvc.perform(get("/user/{userId}/experience", UUID.randomUUID()))
            .andExpect(status().isOk());
    }
}
