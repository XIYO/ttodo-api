package point.ttodoApi.profile.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import point.ttodoApi.profile.application.StatisticsService;
import point.ttodoApi.profile.domain.Statistics;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.user.domain.User;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("StatisticsController 단위 테스트")
@Tag("unit")
@Tag("profile")
@Tag("controller")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @MockitoBean
    private point.ttodoApi.profile.presentation.mapper.ProfilePresentationMapper profilePresentationMapper;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    private static final UUID USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    @Test
    @DisplayName("사용자 통계 조회 성공")
    void getStatistics_Success() throws Exception {
        User owner = User.builder()
            .id(USER_ID)
            .email("user@example.com")
            .password("password")
            .build();
        Statistics statistics = Statistics.builder()
            .owner(owner)
            .succeededTodosCount(10)
            .categoryCount(3)
            .build();
        given(statisticsService.getStatistics(any(UUID.class))).willReturn(statistics);
        given(profilePresentationMapper.toStatisticsResponse(statistics))
            .willCallRealMethod();

        mockMvc.perform(get("/user/{userId}/profile/statistics", USER_ID)
                .with(SecurityMockMvcRequestPostProcessors.user(new TestUserDetails(USER_ID))))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 통계 조회 실패 - 다른 사용자")
    void getStatistics_Forbidden() throws Exception {
        mockMvc.perform(get("/user/{userId}/profile/statistics", USER_ID)
                .with(SecurityMockMvcRequestPostProcessors.user(new TestUserDetails(UUID.randomUUID()))))
            .andExpect(status().isForbidden());
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
