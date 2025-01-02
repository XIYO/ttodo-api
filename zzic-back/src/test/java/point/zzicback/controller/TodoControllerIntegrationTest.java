package point.zzicback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import point.zzicback.dto.request.CreateTodoRequest;
import point.zzicback.dto.request.UpdateTodoRequest;
import point.zzicback.model.Todo;
import point.zzicback.service.TodoService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TodoController 통합 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @PostConstruct
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Nested
    @DisplayName("GET /api/todo")
    class GetAllTodos {

        @Test
        @DisplayName("성공적으로 모든 Todo를 조회함")
        void getAll_success() throws Exception {
            // 데이터 준비
            Todo todo1 = new Todo();
            todo1.setTitle("Test Todo 1");
            todo1.setDescription("Description 1");
            todo1.setDone(false);
            Todo todo2 = new Todo();
            todo2.setTitle("Test Todo 2");
            todo2.setDescription("Description 2");
            todo2.setDone(true);
            todoService.add(todo1);
            todoService.add(todo2);

            // 요청 및 검증
            mockMvc.perform(get("/api/todo"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(Matchers.greaterThanOrEqualTo(2)))
                    .andExpect(jsonPath("$[0].title").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/todo/{id}")
    class GetTodoById {

        @Test
        @DisplayName("성공적으로 특정 Todo를 조회함")
        void getById_success() throws Exception {
            // 데이터 준비
            Todo todo = new Todo();
            todo.setTitle("Test Todo 1");
            todo.setDescription("Description 1");
            todo.setDone(false);
            todoService.add(todo);

            // 요청 및 검증
            mockMvc.perform(get("/api/todo/" + todo.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").exists())
                    .andExpect(jsonPath("$.description").exists());
        }

        @Test
        @DisplayName("해당 ID의 Todo를 찾을 수 없음")
        void getById_notFound() throws Exception {
            mockMvc.perform(get("/api/todo/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/todo")
    class AddTodo {

        @Test
        @DisplayName("성공적으로 Todo를 생성함")
        void add_success() throws Exception {
            CreateTodoRequest request = new CreateTodoRequest();
            request.setTitle("New Todo");
            request.setDescription("Description");

            mockMvc.perform(post("/api/todo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 인해 Todo 생성 실패")
        void add_badRequest() throws Exception {
            CreateTodoRequest request = new CreateTodoRequest(); // 제목 없음

            mockMvc.perform(post("/api/todo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/todo/{id}")
    class ModifyTodo {

        @Test
        @DisplayName("성공적으로 Todo를 수정함")
        void modify_success() throws Exception {
            // 데이터 준비
            Todo todo = new Todo();
            todo.setTitle("Test Todo 1");
            todo.setDescription("Description 1");
            todo.setDone(false);
            todoService.add(todo);

            UpdateTodoRequest request = new UpdateTodoRequest();
            request.setTitle("Updated Todo");
            request.setDescription("Updated Description");
            request.setDone(true);

            mockMvc.perform(put("/api/todo/" + todo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("해당 ID의 Todo를 찾을 수 없음")
        void modify_notFound() throws Exception {
            UpdateTodoRequest request = new UpdateTodoRequest();
            request.setTitle("Updated Todo");
            request.setDescription("Updated Description");
            request.setDone(true);
            request.setTitle("Updated Todo");

            mockMvc.perform(put("/api/todo/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/todo/{id}")
    class RemoveTodo {

        @Test
        @DisplayName("성공적으로 Todo를 삭제함")
        void remove_success() throws Exception {
            // 데이터 준비
            Todo todo = new Todo();
            todo.setTitle("Test Todo 1");
            todo.setDescription("Description 1");
            todo.setDone(false);
            todoService.add(todo);

            mockMvc.perform(delete("/api/todo/" + todo.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("해당 ID의 Todo를 찾을 수 없음")
        void remove_notFound() throws Exception {
            mockMvc.perform(delete("/api/todo/9999"))
                    .andExpect(status().isNotFound());
        }
    }
}
