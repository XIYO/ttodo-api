package point.ttodoApi.todo.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.shared.error.ErrorCode;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.shared.security.AuthorizationService;
import point.ttodoApi.shared.security.CustomPermissionEvaluator;
import point.ttodoApi.todo.application.TodoInstanceService;
import point.ttodoApi.todo.application.TodoSearchService;
import point.ttodoApi.todo.application.TodoTemplateService;
import point.ttodoApi.todo.application.command.*;
import point.ttodoApi.todo.application.query.TodoQuery;
import point.ttodoApi.todo.application.query.TodoSearchQuery;
import point.ttodoApi.todo.application.query.VirtualTodoQuery;
import point.ttodoApi.todo.application.result.TodoResult;
import point.ttodoApi.todo.application.result.TodoStatistics;
import point.ttodoApi.todo.domain.TodoId;
import point.ttodoApi.todo.presentation.dto.request.*;
import point.ttodoApi.todo.presentation.dto.response.TodoResponse;
import point.ttodoApi.todo.presentation.mapper.TodoPresentationMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
@Import({ApiSecurityTestConfig.class, TodoControllerTest.MethodSecurityTestConfig.class})
@DisplayName("TodoController 단위 테스트")
@Tag("unit")
@Tag("todo")
@Tag("controller")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoTemplateService todoTemplateService;

    @MockitoBean
    private TodoInstanceService todoInstanceService;

    @MockitoBean
    private TodoSearchService todoSearchService;

    @MockitoBean
    private TodoPresentationMapper todoPresentationMapper;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    @MockitoBean
    private AuthorizationService authorizationService;

    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 1);

    private TodoResult sampleTodoResult;
    private TodoResponse sampleTodoResponse;

    @BeforeEach
    void setUp() {
        CreateTodoCommand createCommand = new CreateTodoCommand(
            TEST_USER_UUID,
            "Sample Todo",
            "Sample Description",
            false,
            1,
            null,
            TEST_DATE,
            LocalTime.NOON,
            Set.of("work"),
            null
        );
        given(todoPresentationMapper.toCommand(any(CreateTodoRequest.class), any(UUID.class))).willReturn(createCommand);
        doNothing().when(todoTemplateService).createTodo(any(CreateTodoCommand.class));

        UpdateTodoCommand updateCommand = new UpdateTodoCommand(
            TEST_USER_UUID,
            1L,
            0L,
            "Updated Title",
            "Updated Description",
            false,
            2,
            null,
            TEST_DATE.plusDays(1),
            LocalTime.MIDNIGHT,
            Set.of("updated"),
            null
        );
        given(todoPresentationMapper.toCommand(any(UpdateTodoRequest.class), any(UUID.class), anyLong(), anyLong()))
            .willReturn(updateCommand);
        doNothing().when(todoTemplateService).updateTodo(any(UpdateTodoCommand.class));
        doNothing().when(todoTemplateService).partialUpdateTodo(any(UpdateTodoCommand.class));

        UpdateVirtualTodoCommand virtualCommand = new UpdateVirtualTodoCommand(
            "1:1",
            TEST_USER_UUID,
            "Virtual Title",
            "Virtual Description",
            true,
            1,
            null,
            TEST_DATE.plusDays(1),
            LocalTime.NOON,
            Set.of("virtual")
        );
        given(todoPresentationMapper.toVirtualCommand(any(UpdateTodoRequest.class), any(UUID.class), anyString()))
            .willReturn(virtualCommand);
        doReturn(false).when(todoInstanceService).existsVirtualTodo(any(UUID.class), any(TodoId.class));
        doReturn(virtualCommand).when(todoPresentationMapper).toVirtualCommand(any(UpdateTodoRequest.class), eq(TEST_USER_UUID), anyString());
        doReturn(createTodoResult()).when(todoInstanceService).updateOrCreateVirtualTodo(any(UpdateVirtualTodoCommand.class));
        doNothing().when(todoInstanceService).deactivateVirtualTodo(any(DeleteTodoCommand.class));
        doNothing().when(todoTemplateService).deactivateTodo(any(DeleteTodoCommand.class));

        sampleTodoResult = createTodoResult();
        sampleTodoResponse = createTodoResponse();

        given(todoPresentationMapper.toResponse(any(TodoResult.class))).willReturn(sampleTodoResponse);
        given(todoTemplateService.getTodo(any(TodoQuery.class))).willReturn(sampleTodoResult);
        given(todoInstanceService.getVirtualTodo(any(VirtualTodoQuery.class))).willReturn(sampleTodoResult);

        TodoSearchQuery searchQuery = new TodoSearchQuery(
            TEST_USER_UUID,
            null,
            List.of(),
            List.of(),
            List.of(),
            null,
            null,
            null,
            null,
            PageRequest.of(0, 10)
        );
        given(todoPresentationMapper.toQuery(any(TodoSearchRequest.class), any(UUID.class))).willReturn(searchQuery);

        Page<TodoResult> todoPage = new PageImpl<>(List.of(sampleTodoResult));
        given(todoInstanceService.getTodoList(any(TodoSearchQuery.class))).willReturn(todoPage);

        given(todoPresentationMapper.isOnlyCompleteFieldUpdate(any(UpdateTodoRequest.class))).willReturn(false);

        TodoStatistics statistics = new TodoStatistics(5, 3, 2);
        given(todoInstanceService.getTodoStatistics(eq(TEST_USER_UUID), any(LocalDate.class))).willReturn(statistics);

        given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString()))
            .willReturn(true);
    }

    private TodoResult createTodoResult() {
        return new TodoResult(
            "1:0",
            "Sample Todo",
            "Sample Description",
            false,
            false,
            0,
            1,
            "Medium",
            null,
            null,
            TEST_DATE,
            LocalTime.NOON,
            null,
            TEST_DATE,
            Set.of("work"),
            1L
        );
    }

    private TodoResponse createTodoResponse() {
        return new TodoResponse(
            "1:0",
            "Sample Todo",
            "Sample Description",
            false,
            false,
            0,
            1,
            "Medium",
            null,
            null,
            TEST_DATE,
            LocalTime.NOON,
            null,
            TEST_DATE,
            Set.of("work"),
            1L
        );
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
    @DisplayName("1. CREATE - TODO 생성")
    class CreateTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("TODO 생성 성공 - 필수 필드만")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Success_Minimal() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test TODO")
                        .param("priorityId", "1"))
                    .andExpect(status().isCreated());
            }

            @Test
            @DisplayName("TODO 생성 성공 - 모든 필드")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Success_AllFields() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Complete TODO")
                        .param("description", "Test Description")
                        .param("priorityId", "2")
                        .param("date", "2025-02-01"))
                    .andExpect(status().isCreated());
            }
        }

        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {

            @Test
            @DisplayName("TODO 생성 실패 - 인증 없음")
            void createTodo_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test TODO")
                        .param("priorityId", "1"))
                    .andExpect(status().isForbidden());
            }
        }

        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {

            @Test
            @DisplayName("TODO 생성 실패 - 제목 미입력")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Failure_NoTitle() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("priorityId", "1"))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("2. READ - TODO 조회")
    class ReadTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("TODO 목록 조회 성공")
            @WithMockUser(username = TEST_USER_ID)
            void getTodos_Success() throws Exception {
                mockMvc.perform(get("/todos"))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("TODO 상세 조회 성공 - 원본")
            @WithMockUser(username = TEST_USER_ID)
            void getTodoTemplate_Success() throws Exception {
                mockMvc.perform(get("/todos/1:0"))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("TODO 상세 조회 성공 - 가상")
            @WithMockUser(username = TEST_USER_ID)
            void getTodoVirtual_Success() throws Exception {
                mockMvc.perform(get("/todos/1:1"))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("TODO 통계 조회 성공")
            @WithMockUser(username = TEST_USER_ID)
            void getTodoStatistics_Success() throws Exception {
                mockMvc.perform(get("/todos/statistics")
                        .param("date", "2025-03-01"))
                    .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {

            @Test
            @DisplayName("TODO 목록 조회 실패 - 인증 없음")
            void getTodos_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get("/todos"))
                    .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("TODO 상세 조회 실패 - 인증 없음")
            void getTodo_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get("/todos/1:0"))
                    .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("TODO 통계 조회 실패 - 인증 없음")
            void getStatistics_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get("/todos/statistics"))
                    .andExpect(status().isForbidden());
            }
        }

        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {

            @Test
            @DisplayName("TODO 상세 조회 실패 - 존재하지 않는 TODO")
            @WithMockUser(username = TEST_USER_ID)
            void getTodo_Failure_NotFound() throws Exception {
                given(todoInstanceService.getVirtualTodo(any(VirtualTodoQuery.class)))
                    .willThrow(new BusinessException(ErrorCode.TODO_NOT_FOUND));

                mockMvc.perform(get("/todos/999:1"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("3. UPDATE - TODO 수정")
    class UpdateTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("TODO 수정 성공")
            @WithMockUser(username = TEST_USER_ID)
            void updateTodo_Success() throws Exception {
                mockMvc.perform(put("/todos/1:0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated"))
                    .andExpect(status().isNoContent());
            }

            @Test
            @DisplayName("TODO 완료 토글 성공")
            @WithMockUser(username = TEST_USER_ID)
            void patchTodo_Complete_Success() throws Exception {
                given(todoPresentationMapper.isOnlyCompleteFieldUpdate(any(UpdateTodoRequest.class))).willReturn(true);

                mockMvc.perform(patch("/todos/1:0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("complete", "true"))
                    .andExpect(status().isNoContent());
            }
        }

        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {

            @Test
            @DisplayName("TODO 수정 실패 - 인증 없음")
            void updateTodo_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(put("/todos/1:0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated"))
                    .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("4. DELETE - TODO 삭제")
    class DeleteTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("TODO 삭제 성공")
            @WithMockUser(username = TEST_USER_ID)
            void deleteTodo_Success() throws Exception {
                mockMvc.perform(delete("/todos/1:0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("deleteAll", "false"))
                    .andExpect(status().isNoContent());
            }
        }

        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {

            @Test
            @DisplayName("TODO 삭제 실패 - 존재하지 않는 TODO")
            @WithMockUser(username = TEST_USER_ID)
            void deleteTodo_Failure_NotFound() throws Exception {
                doThrow(new BusinessException(ErrorCode.TODO_NOT_FOUND))
                    .when(todoInstanceService).deactivateVirtualTodo(any(DeleteTodoCommand.class));

                mockMvc.perform(delete("/todos/1:1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("deleteAll", "false"))
                    .andExpect(status().isNotFound());
            }
        }
    }

}
