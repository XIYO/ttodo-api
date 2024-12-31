package point.zzicback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import point.zzicback.mapper.TodoMapper;
import point.zzicback.service.TodoService;

/**
 * TodoController 통합 테스트
 */
@WebMvcTest(TodoController.class)
class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/todo")
    class GetTodoList {

        @Test
        @DisplayName("성공적으로 Todo 목록을 조회함")
        void getTodoList_success() throws Exception {
        }
    }

    @Nested
    @DisplayName("GET /api/todo/{id}")
    class GetTodoById {

        @Test
        @DisplayName("성공적으로 특정 Todo를 조회함")
        void getTodoById_success() throws Exception {
        }

        @Test
        @DisplayName("해당 ID의 Todo를 찾을 수 없음")
        void getTodoById_notFound() throws Exception {
        }
    }

    @Nested
    @DisplayName("POST /api/todo")
    class CreateTodo {

        @Test
        @DisplayName("성공적으로 Todo를 생성함")
        void createTodo_success() {
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 인해 Todo 생성 실패")
        void createTodo_badRequest() {
        }
    }

    @Nested
    @DisplayName("PUT /api/todo/{id}")
    class UpdateTodo {

        @Test
        @DisplayName("성공적으로 Todo를 수정함")
        void updateTodo_success() {
        }

        @Test
        @DisplayName("해당 ID의 Todo를 찾을 수 없음")
        void updateTodo_notFound() {
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 인해 Todo 수정 실패")
        void updateTodo_badRequest() {
        }
    }

    @Nested
    @DisplayName("DELETE /api/todo/{id}")
    class DeleteTodo {

        @Test
        @DisplayName("성공적으로 Todo를 삭제함")
        void deleteTodo_success() {
        }

        @Test
        @DisplayName("해당 ID의 Todo를 찾을 수 없음")
        void deleteTodo_notFound() {
        }
    }
}
