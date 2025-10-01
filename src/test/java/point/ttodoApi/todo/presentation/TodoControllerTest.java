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
    private TodoCommandService todoCommandService;
    
    @MockitoBean
    private TodoQueryService todoQueryService;
    
    @MockitoBean
    private TodoSearchService todoSearchService;
    
    @MockitoBean
    private TodoPresentationMapper todoPresentationMapper;
    
    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    private static final String BASE_URL = "/todos";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final UUID TEST_USER_UUID = UUID.fromString(TEST_USER_ID);
    
    @BeforeEach
    void setUp() {
        // Mock 설정은 각 테스트에서 필요시 개별적으로 수행
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
                mockMvc.perform(post(BASE_URL)
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
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Complete TODO")
                        .param("description", "Test Description")
                        .param("priorityId", "1")
                        .param("date", "2025-01-15")
                        .param("categoryId", "1"))
                    .andExpect(status().isCreated());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("TODO 생성 실패 - 인증 없음")
            void createTodo_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(post(BASE_URL)
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
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createTodo_Failure_NoTitle() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("priorityId", "1"))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("TODO 생성 실패 - 우선순위 미입력")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createTodo_Failure_NoPriority() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test TODO"))
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
                mockMvc.perform(post(BASE_URL)
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
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("TODO 상세 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getTodo_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("TODO 통계 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getTodoStatistics_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/statistics"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("TODO 목록 조회 실패 - 인증 없음")
            void getTodos_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
            }
            
            @Test
            @DisplayName("TODO 상세 조회 실패 - 인증 없음")
            void getTodo_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
            }
            
            @Test
            @DisplayName("TODO 통계 조회 실패 - 인증 없음")
            void getTodoStatistics_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get(BASE_URL + "/statistics"))
                    .andExpect(status().isForbidden());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("TODO 상세 조회 실패 - 존재하지 않는 TODO")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getTodo_Failure_NotFound() throws Exception {
                // 간소화된 테스트 - 실제 서비스 메서드 확인 후 수정 필요
                mockMvc.perform(get(BASE_URL + "/999"))
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
                mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated TODO")
                        .param("description", "Updated Description")
                        .param("priorityId", "2"))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("TODO 완료 토글 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void toggleTodoComplete_Success() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/1/complete"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("TODO 수정 실패 - 인증 없음")
            void updateTodo_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated TODO"))
                    .andExpect(status().isForbidden());
            }
            
            @Test
            @DisplayName("TODO 완료 토글 실패 - 인증 없음")
            void toggleTodoComplete_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/1/complete"))
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
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void deleteTodo_Success() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("TODO 삭제 실패 - 인증 없음")
            void deleteTodo_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("TODO 삭제 실패 - 존재하지 않는 TODO")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void deleteTodo_Failure_NotFound() throws Exception {
                // 간소화된 테스트 - 실제 예외 처리 확인 후 수정 필요
                mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());
            }
        }
    }

}