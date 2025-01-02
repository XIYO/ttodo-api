package point.zzicback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
@DisplayName("TodoController 통합 테스트")
@AutoConfigureMockMvc
class TodoRestControllerIntegrationTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;


    Todo todo1 = new Todo();
    Todo todo2 = new Todo();

    /**
     * 테스트용 더미 데이터 생성
     */
    @PostConstruct
    public void initTestData() {
        todo1.setTitle("Test Todo 1");
        todo1.setDescription("Description 1");
        todo1.setDone(false);

        todo2.setTitle("Test Todo 2");
        todo2.setDescription("Description 2");
        todo2.setDone(true);

        todoService.add(todo1);
        todoService.add(todo2);
    }

    @Nested
    @DisplayName("GET /api/todo")
    class GetAllTodos {

        @Test
        @DisplayName("성공적으로 모든 Todo를 조회함")
        void getAll_success() throws Exception {
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
            // 요청 및 검증
            mockMvc.perform(get("/api/todo/" + todo1.getId()))
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
            UpdateTodoRequest request = new UpdateTodoRequest();
            request.setTitle("Updated Todo");
            request.setDescription("Updated Description");
            request.setDone(true);

            mockMvc.perform(put("/api/todo/" + todo1.getId())
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
            mockMvc.perform(delete("/api/todo/" + todo1.getId()))
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
