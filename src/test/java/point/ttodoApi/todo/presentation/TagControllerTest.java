package point.ttodoApi.todo.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.security.AuthorizationService;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.todo.application.TodoTemplateService;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@Import({ApiSecurityTestConfig.class, TagControllerTest.MethodSecurityConfig.class})
@DisplayName("TagController 단위 테스트")
@Tag("unit")
@Tag("todo")
@Tag("controller")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoTemplateService todoTemplateService;

    @MockitoBean
    private AuthorizationService authorizationService;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    private static final String USERNAME = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final UUID USER_ID = UUID.fromString(USERNAME);

    @BeforeEach
    void setUp() {
        given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(true);
    }

    @TestConfiguration
    static class MethodSecurityConfig {
        @Bean
        PermissionEvaluator testPermissionEvaluator(AuthorizationService authorizationService) {
            return new PermissionEvaluator() {
                @Override
                public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
                    try {
                        UUID userId = UUID.fromString(authentication.getName());
                        return authorizationService.hasPermission(userId, null, "Tag", permission.toString());
                    } catch (Exception ex) {
                        return false;
                    }
                }

                @Override
                public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
                    try {
                        UUID userId = UUID.fromString(authentication.getName());
                        return authorizationService.hasPermission(userId, targetId, targetType, permission.toString());
                    } catch (Exception ex) {
                        return false;
                    }
                }
            };
        }

        @Bean
        MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator testPermissionEvaluator) {
            DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
            handler.setPermissionEvaluator(testPermissionEvaluator);
            return handler;
        }
    }

    @Nested
    @DisplayName("1. READ - 태그 목록 조회")
    class ReadTests {

        @Test
        @DisplayName("태그 목록 조회 성공")
        @WithMockUser(username = USERNAME)
        void getTags_Success() throws Exception {
            Page<String> tags = new PageImpl<>(List.of("work", "study"), PageRequest.of(0, 10), 2);
            given(todoTemplateService.getTags(eq(USER_ID), any(), any(Pageable.class))).willReturn(tags);

            mockMvc.perform(get("/tags")
                    .param("page", "0")
                    .param("size", "10")
                    .param("direction", "asc"))
                .andExpect(status().isOk());

            verify(todoTemplateService).getTags(eq(USER_ID), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("태그 목록 조회 실패 - 권한 없음")
        @WithMockUser(username = USERNAME)
        void getTags_Forbidden() throws Exception {
            given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(false);

            mockMvc.perform(get("/tags"))
                .andExpect(status().isForbidden());

            given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(true);
        }
    }
}
