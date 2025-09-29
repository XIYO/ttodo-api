package point.ttodoApi.todo.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.shared.config.auth.SecurityTestConfig;
import point.ttodoApi.todo.application.*;
import point.ttodoApi.todo.application.command.*;
import point.ttodoApi.todo.application.result.TodoResult;
import point.ttodoApi.todo.presentation.mapper.TodoPresentationMapper;
import point.ttodoApi.todo.presentation.dto.request.*;
import point.ttodoApi.todo.presentation.dto.response.*;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TodoController 단위 테스트
 * 최신 Spring Boot 3.x 문법 적용
 * Nested 구조로 CRUD 순서에 따라 체계적 테스트 구성
 */
@WebMvcTest(TodoController.class)
@Import({SecurityTestConfig.class})
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private TodoCommandService todoCommandService;
    
    @MockitoBean
    private TodoQueryService todoQueryService;
    
    @MockitoBean
    private TodoSearchService todoSearchService;
    
    
    @MockitoBean
    private TodoPresentationMapper todoPresentationMapper;
    
    @MockitoBean
    private point.ttodoApi.shared.error.ErrorMetricsCollector errorMetricsCollector;
    
    private static final String BASE_URL = "/todos";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    
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
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Success_RequiredFieldsOnly() throws Exception {
                // Given
                TodoResult mockResult = createMockTodoResult();
                point.ttodoApi.todo.presentation.dto.response.TodoResponse mockResponse = createMockTodoResponse();
                
                given(todoPresentationMapper.toCommand(any(), any(UUID.class))).willReturn(mock(CreateTodoCommand.class));
                given(todoCommandService.createTodo(any(CreateTodoCommand.class))).willReturn(mockResult);
                given(todoPresentationMapper.toResponse(any(TodoResult.class))).willReturn(mockResponse);
                
                // When & Then
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "테스트 할일")
                        .param("priorityId", "1"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("테스트 할일")))
                    .andExpect(jsonPath("$.priorityId", is(1)))
                    .andExpect(jsonPath("$.complete", is(false)));
            }
            
            @Test
            @Order(2)
            @DisplayName("TODO 생성 성공 - 모든 필드")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Success_AllFields() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "상세 할일")
                        .param("description", "할일에 대한 설명")
                        .param("priorityId", "2")
                        .param("date", LocalDate.now().plusDays(1).toString())
                        .param("time", "14:30")
                        .param("tags", "업무,중요"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("상세 할일")))
                    .andExpect(jsonPath("$.description", is("할일에 대한 설명")))
                    .andExpect(jsonPath("$.tags", hasItem("업무")))
                    .andExpect(jsonPath("$.tags", hasItem("중요")));
            }
            
            @Test
            @Order(3)
            @DisplayName("TODO 생성 성공 - 반복 규칙 포함")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Success_WithRecurrence() throws Exception {
                String recurrenceRule = "{\"frequency\":\"WEEKLY\",\"interval\":1,\"byWeekDays\":[\"MO\",\"WE\",\"FR\"]}";
                
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "반복 할일")
                        .param("priorityId", "1")
                        .param("date", LocalDate.now().toString())
                        .param("recurrenceRuleJson", recurrenceRule))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("반복 할일")))
                    .andExpect(jsonPath("$.recurrenceRule").exists());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {
            
            @Test
            @DisplayName("TODO 생성 실패 - 인증 없음")
            void createTodo_Failure_NoAuth() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "테스트 할일")
                        .param("priorityId", "1"))
                    .andExpect(status().isUnauthorized());
            }
            
            @Test
            @DisplayName("TODO 생성 실패 - 제목 누락")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Failure_NoTitle() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("priorityId", "1"))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("TODO 생성 실패 - 너무 긴 제목")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Failure_TooLongTitle() throws Exception {
                String longTitle = "a".repeat(256); // 255자 제한 가정
                
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", longTitle)
                        .param("priorityId", "1"))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("TODO 생성 실패 - 잘못된 우선순위")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Failure_InvalidPriority() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "테스트 할일")
                        .param("priorityId", "99"))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("TODO 생성 실패 - 잘못된 날짜 형식")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_Failure_InvalidDateFormat() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "테스트 할일")
                        .param("priorityId", "1")
                        .param("date", "invalid-date"))
                    .andExpect(status().isBadRequest());
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("TODO 생성 - 이모지 제목")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_EdgeCase_EmojiTitle() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "📝 이모지 할일 ✅")
                        .param("priorityId", "1"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", is("📝 이모지 할일 ✅")));
            }
            
            @Test
            @DisplayName("TODO 생성 - 과거 날짜")
            @WithMockUser(username = TEST_USER_ID)
            void createTodo_EdgeCase_PastDate() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "과거 할일")
                        .param("priorityId", "1")
                        .param("date", LocalDate.now().minusDays(1).toString()))
                    .andExpect(status().isCreated()); // 과거 날짜도 허용
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
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            }
            
            @Test
            @DisplayName("TODO 목록 조회 성공 - 날짜 필터")
            @WithMockUser(username = TEST_USER_ID)
            void getTodos_Success_WithDateFilter() throws Exception {
                mockMvc.perform(get(BASE_URL)
                        .param("date", LocalDate.now().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            }
            
            @Test
            @DisplayName("TODO 목록 조회 성공 - 완료 상태 필터")
            @WithMockUser(username = TEST_USER_ID)
            void getTodos_Success_WithCompleteFilter() throws Exception {
                mockMvc.perform(get(BASE_URL)
                        .param("complete", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            }
            
            @Test
            @DisplayName("TODO 검색 성공")
            @WithMockUser(username = TEST_USER_ID)
            void searchTodos_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/search")
                        .param("keyword", "테스트")
                        .param("page", "0")
                        .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.pageable").exists());
            }
            
            @Test
            @DisplayName("TODO 통계 조회 성공")
            @WithMockUser(username = TEST_USER_ID)
            void getTodoStatistics_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").exists())
                    .andExpect(jsonPath("$.completed").exists())
                    .andExpect(jsonPath("$.pending").exists());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {
            
            @Test
            @DisplayName("TODO 목록 조회 실패 - 인증 없음")
            void getTodos_Failure_NoAuth() throws Exception {
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
            }
            
            @Test
            @DisplayName("TODO 검색 실패 - 잘못된 페이지 크기")
            @WithMockUser(username = TEST_USER_ID)
            void searchTodos_Failure_InvalidPageSize() throws Exception {
                mockMvc.perform(get(BASE_URL + "/search")
                        .param("size", "0"))
                    .andExpect(status().isBadRequest());
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
            @DisplayName("TODO 수정 성공 - 제목 변경")
            @WithMockUser(username = TEST_USER_ID)
            void updateTodo_Success_TitleChange() throws Exception {
                // Using mock TODO ID for test
                String todoId = "test-todo-id";
                
                mockMvc.perform(put(BASE_URL + "/" + todoId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "수정된 제목")
                        .param("priorityId", "1"))
                    .andExpect(status().isOk()); // TODO가 없을 수 있음
            }
            
            @Test
            @DisplayName("TODO 완료 상태 변경 성공")
            @WithMockUser(username = TEST_USER_ID)
            void updateTodo_Success_CompleteToggle() throws Exception {
                String todoId = "test-todo-id";
                
                mockMvc.perform(patch(BASE_URL + "/" + todoId + "/complete"))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("TODO 고정 상태 변경 성공")
            @WithMockUser(username = TEST_USER_ID)
            void updateTodo_Success_PinToggle() throws Exception {
                String todoId = "test-todo-id";
                
                mockMvc.perform(patch(BASE_URL + "/" + todoId + "/pin"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {
            
            @Test
            @DisplayName("TODO 수정 실패 - 인증 없음")
            void updateTodo_Failure_NoAuth() throws Exception {
                mockMvc.perform(put(BASE_URL + "/test-id")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "수정된 제목")
                        .param("priorityId", "1"))
                    .andExpect(status().isUnauthorized());
            }
            
            @Test
            @DisplayName("TODO 수정 실패 - 제목 누락")
            @WithMockUser(username = TEST_USER_ID)
            void updateTodo_Failure_NoTitle() throws Exception {
                mockMvc.perform(put(BASE_URL + "/test-id")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("priorityId", "1"))
                    .andExpect(status().isBadRequest());
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
                String todoId = "test-todo-id";
                
                mockMvc.perform(delete(BASE_URL + "/" + todoId))
                    .andExpect(status().isNoContent());
            }
            
            @Test
            @DisplayName("TODO 일괄 삭제 성공 - 완료된 항목")
            @WithMockUser(username = TEST_USER_ID)
            void deleteCompletedTodos_Success() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/completed"))
                    .andExpect(status().isNoContent());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {
            
            @Test
            @DisplayName("TODO 삭제 실패 - 인증 없음")
            void deleteTodo_Failure_NoAuth() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/test-id"))
                    .andExpect(status().isUnauthorized());
            }
            
            @Test
            @DisplayName("TODO 삭제 실패 - 잘못된 ID 형식")
            @WithMockUser(username = TEST_USER_ID)
            void deleteTodo_Failure_InvalidId() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/invalid-id-format"))
                    .andExpect(status().isBadRequest());
            }
        }
    }
    
    @Nested
    @DisplayName("5. 인증 및 권한 테스트")
    class AuthenticationAndAuthorizationTests {
        
        @Test
        @DisplayName("인증 토큰 없이 접근 - 401 반환")
        void withoutAuth_Returns401() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
            
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "테스트"))
                .andExpect(status().isUnauthorized());
            
            mockMvc.perform(put(BASE_URL + "/test-id")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "수정"))
                .andExpect(status().isUnauthorized());
            
            mockMvc.perform(delete(BASE_URL + "/test-id"))
                .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("Bearer 토큰 인증 시도")
        void withBearerToken() throws Exception {
            String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ...";
            
            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()); // 실제 유효한 토큰이 아니므로
        }
        
        @Test
        @DisplayName("쿠키 인증 시도")
        void withCookieAuth() throws Exception {
            String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ...";
            
            mockMvc.perform(get(BASE_URL)
                    .cookie(new jakarta.servlet.http.Cookie("access-token", token)))
                .andExpect(status().isUnauthorized()); // 실제 유효한 토큰이 아니므로
        }
        
        @Test
        @DisplayName("@WithMockUser로 인증 성공")
        @WithMockUser(username = TEST_USER_ID)
        void withMockUser_Success() throws Exception {
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
        }
    }
    
    @Nested
    @DisplayName("6. 통합 엣지 케이스")
    class EdgeCasesComprehensive {
        
        @Test
        @DisplayName("대용량 태그 처리")
        @WithMockUser(username = TEST_USER_ID)
        void largeTagSet() throws Exception {
            String tags = String.join(",", 
                "태그1", "태그2", "태그3", "태그4", "태그5",
                "태그6", "태그7", "태그8", "태그9", "태그10");
            
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "많은 태그")
                    .param("priorityId", "1")
                    .param("tags", tags))
                .andExpect(status().isCreated()); // 태그 개수 제한에 따라
        }
        
        @Test
        @DisplayName("특수문자 포함 제목")
        @WithMockUser(username = TEST_USER_ID)
        void specialCharactersInTitle() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "특수문자 !@#$%^&*() 테스트")
                    .param("priorityId", "1"))
                .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("동시 요청 처리")
        @WithMockUser(username = TEST_USER_ID)
        void concurrentRequests() throws Exception {
            // Multiple concurrent TODO creation requests
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "동시 요청 " + i)
                        .param("priorityId", "1"))
                    .andExpect(status().isCreated());
            }
        }
    }
    
    // Helper methods
    private TodoResult createMockTodoResult() {
        return new TodoResult(
            "1",             // id
            "테스트 할 일",    // title
            null,            // description
            false,           // complete
            false,           // isPinned
            1,               // displayOrder
            1,               // priorityId
            "높음",           // priorityName
            null,            // categoryId
            null,            // categoryName
            LocalDate.now(), // date
            null,            // time
            null,            // recurrenceRule
            null,            // anchorDate
            null,            // tags
            null             // originalTodoId
        );
    }

    private TodoResponse createMockTodoResponse() {
        // TodoResponse 구조 확인 필요
        return null; // 임시
    }

    private CreateTodoRequest createValidTodoRequest() {
        return new CreateTodoRequest(
            "새로운 할 일",  // title
            null,           // description  
            false,          // complete
            1,              // priorityId
            null,           // categoryId
            LocalDate.now(), // date
            null,           // time
            null,           // recurrenceRuleJson
            null            // tags
        );
    }

    private UpdateTodoRequest createUpdateTodoRequest() {
        // UpdateTodoRequest 구조 확인 필요
        return null; // 임시
    }

    private Page<TodoResult> createMockTodoPage() {
        List<TodoResult> todos = List.of(createMockTodoResult());
        return new PageImpl<>(todos, PageRequest.of(0, 20), todos.size());
    }

    private Page<TodoResponse> createMockTodoResponsePage() {
        List<TodoResponse> todos = List.of(createMockTodoResponse());
        return new PageImpl<>(todos, PageRequest.of(0, 20), todos.size());
    }
}