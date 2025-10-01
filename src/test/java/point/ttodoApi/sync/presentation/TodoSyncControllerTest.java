package point.ttodoApi.sync.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoSyncController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("TodoSyncController 단위 테스트")
@Tag("unit")
@Tag("sync")
@Tag("controller")
class TodoSyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("1. GET /api/todos")
    class GetTodos {
        @Test
        @DisplayName("전체 TODO 조회 성공")
        void getAllTodos_Success() throws Exception {
            when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(Map.of("id", 1)));

            mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk());

            verify(jdbcTemplate).queryForList(anyString());
        }
    }

    @Nested
    @DisplayName("2. POST /api/todos")
    class CreateTodo {
        @Test
        @DisplayName("TODO 생성 성공")
        void createTodo_Success() throws Exception {
            when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any())).thenReturn(1);

            var payload = Map.of(
                "id", "1",
                "title", "Test",
                "description", "Desc",
                "complete", Boolean.FALSE,
                "created_at", 1700000000000L
            );

            mockMvc.perform(post("/api/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

            verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("3. PUT /api/todos/{id}")
    class UpdateTodo {
        @Test
        @DisplayName("TODO 수정 성공")
        void updateTodo_Success() throws Exception {
            when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

            var payload = Map.of(
                "title", "Updated",
                "description", "Desc",
                "complete", Boolean.TRUE
            );

            mockMvc.perform(put("/api/todos/{id}", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

            verify(jdbcTemplate).update(anyString(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("4. DELETE /api/todos/{id}")
    class DeleteTodo {
        @Test
        @DisplayName("TODO 삭제 성공")
        void deleteTodo_Success() throws Exception {
            when(jdbcTemplate.update(anyString(), anyInt())).thenReturn(1);

            mockMvc.perform(delete("/api/todos/{id}", "1"))
                .andExpect(status().isOk());

            verify(jdbcTemplate).update(anyString(), anyInt());
        }
    }
}
