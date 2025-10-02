package point.ttodoApi.todo.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.*;
import point.ttodoApi.todo.application.*;
import point.ttodoApi.todo.application.command.*;
import point.ttodoApi.todo.presentation.mapper.TodoPresentationMapper;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TodoController 단위 테스트
 * 개별 Mock 방식 적용
 * @WithMockUser 기반 인증 처리
 * CRUD 순서 + Nested 구조 + 한글 DisplayName
 */
@WebMvcTest(TodoController.class)
@Import(ApiSecurityTestConfig.class)
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

    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    
    @BeforeEach
    void setUp() {
        // 기본 TodoResult 생성
        point.ttodoApi.todo.application.result.TodoResult todoResult =
            new point.ttodoApi.todo.application.result.TodoResult(
                "1:0", "Test TODO", null, false, false, 0, 1, "보통", null, null, null, null, null, null, null, 1L
            );

        // 기본 TodoResponse 생성
        point.ttodoApi.todo.presentation.dto.response.TodoResponse todoResponse =
            new point.ttodoApi.todo.presentation.dto.response.TodoResponse(
                "1:0", "Test TODO", null, false, false, 0, 1, "보통", null, null, null, null, null, null, null, 1L
            );

        // TodoSearchQuery 생성
        point.ttodoApi.todo.application.query.TodoSearchQuery searchQuery =
            new point.ttodoApi.todo.application.query.TodoSearchQuery(
                TEST_USER_UUID, null, null, null, null, null, null, null, null, org.springframework.data.domain.Pageable.unpaged()
            );

        // Mapper mocks - willReturn any command/query
        given(todoPresentationMapper.toCommand(any(point.ttodoApi.todo.presentation.dto.request.CreateTodoRequest.class), any()))
            .willAnswer(inv -> new point.ttodoApi.todo.application.command.CreateTodoCommand(
                TEST_USER_UUID, "Test TODO", null, false, 1, null, null, null, null, null
            ));

        given(todoPresentationMapper.toCommand(any(point.ttodoApi.todo.presentation.dto.request.UpdateTodoRequest.class), any(), any(), any()))
            .willAnswer(inv -> new point.ttodoApi.todo.application.command.UpdateTodoCommand(
                TEST_USER_UUID, 1L, 0L, "Updated TODO", null, false, 1, null, null, null, null, null
            ));

        given(todoPresentationMapper.toVirtualCommand(any(), any(), any()))
            .willAnswer(inv -> new point.ttodoApi.todo.application.command.UpdateVirtualTodoCommand(
                "1:3", TEST_USER_UUID, "Updated TODO", null, false, 1, null, null, null, null
            ));

        given(todoPresentationMapper.toQuery(any(), any()))
            .willReturn(searchQuery);

        given(todoPresentationMapper.toResponse(any(point.ttodoApi.todo.application.result.TodoResult.class)))
            .willReturn(todoResponse);

        given(todoPresentationMapper.isOnlyCompleteFieldUpdate(any()))
            .willReturn(false);

        // TodoTemplateService mocks
        doNothing().when(todoTemplateService).createTodo(any());
        doNothing().when(todoTemplateService).updateTodo(any());
        doNothing().when(todoTemplateService).partialUpdateTodo(any());
        doNothing().when(todoTemplateService).deactivateTodo(any());
        given(todoTemplateService.getTodo(any())).willReturn(todoResult);

        // TodoInstanceService mocks
        given(todoInstanceService.getTodoList(any())).willReturn(org.springframework.data.domain.Page.empty());
        given(todoInstanceService.getVirtualTodo(any())).willReturn(todoResult);
        given(todoInstanceService.updateOrCreateVirtualTodo(any())).willReturn(todoResult);
        given(todoInstanceService.existsVirtualTodo(any(), any())).willReturn(false);
        doNothing().when(todoInstanceService).deactivateVirtualTodo(any());
        given(todoInstanceService.getTodoStatistics(any(), any()))
            .willReturn(new point.ttodoApi.todo.application.result.TodoStatistics(10L, 5L, 5L));

        // TodoSearchService mocks (if needed)
    }

    @Nested
    @DisplayName("1. CREATE - TODO 생성")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @Order(1)
            @DisplayName("TODO 생성 성공 - 필수 필드만")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createTodo_Success_RequiredFieldsOnly() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test TODO")
                        .param("priorityId", "1"))
                    .andExpect(status().isCreated());
            }

            @Test
            @Order(2)
            @DisplayName("TODO 생성 성공 - 모든 필드")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createTodo_Success_AllFields() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Complete TODO")
                        .param("description", "Test Description")
                        .param("priorityId", "1")
                        .param("date", "2025-01-15")
                        .param("categoryId", "550e8400-e29b-41d4-a716-446655440000"))
                    .andExpect(status().isCreated());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("TODO 생성 실패 - 제목 미입력")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createTodo_Failure_NoTitle() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("priorityId", "1"))
                    .andExpect(status().isBadRequest());
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("TODO 생성 - 설명 미입력시 기본값 사용")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createTodo_EdgeCase_NoDescription() throws Exception {
                mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test TODO")
                        .param("priorityId", "1"))
                    .andExpect(status().isCreated());
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
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getTodos_Success() throws Exception {
                mockMvc.perform(get("/todos"))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("TODO 상세 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getTodo_Success() throws Exception {
                mockMvc.perform(get("/todos/1:0"))
                    .andExpect(status().isOk());
            }

            @Test
            @DisplayName("TODO 통계 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getTodoStatistics_Success() throws Exception {
                mockMvc.perform(get("/todos/statistics"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("TODO 상세 조회 실패 - 존재하지 않는 TODO")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getTodo_Failure_NotFound() throws Exception {
                // Mock service to throw EntityNotFoundException
                given(todoTemplateService.getTodo(any()))
                    .willThrow(new point.ttodoApi.shared.error.EntityNotFoundException("Todo", 999L));

                mockMvc.perform(get("/todos/999:0"))
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
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void updateTodo_Success() throws Exception {
                mockMvc.perform(put("/todos/1:0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated TODO")
                        .param("description", "Updated Description")
                        .param("priorityId", "2"))
                    .andExpect(status().isNoContent());
            }

            @Test
            @DisplayName("TODO 완료 토글 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void toggleTodoComplete_Success() throws Exception {
                mockMvc.perform(patch("/todos/1:0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("complete", "true"))
                    .andExpect(status().isNoContent());
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
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
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
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void deleteTodo_Failure_NotFound() throws Exception {
                // Mock service to throw EntityNotFoundException
                doThrow(new point.ttodoApi.shared.error.EntityNotFoundException("Todo", 999L))
                    .when(todoInstanceService).deactivateVirtualTodo(any());

                mockMvc.perform(delete("/todos/999:0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("deleteAll", "false"))
                    .andExpect(status().isNotFound());
            }
        }
    }

}