package point.ttodoApi.sync.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TodoSyncController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Map<String, Object>> getAllTodos() {
        log.info("Fetching all todos for sync");
        
        String sql = "SELECT id, title, description, complete, created_at, created_at as updated_at FROM simple_todo ORDER BY created_at DESC";
        
        List<Map<String, Object>> todos = jdbcTemplate.queryForList(sql);
        log.info("Returning {} todos", todos.size());
        
        return todos;
    }
    
    @PostMapping
    @PreAuthorize("permitAll()")
    public Map<String, Object> createTodo(@RequestBody Map<String, Object> todo) {
        log.info("Creating todo: {}", todo);
        
        // updated_at 컬럼이 없으므로 created_at만 사용
        String sql = "INSERT INTO simple_todo (id, title, description, complete, created_at) VALUES (?, ?, ?, ?, ?)";
        
        // ID를 integer로 변환
        Integer id = Integer.parseInt(todo.get("id").toString());
        
        // Convert created_at from number to timestamp
        Object createdAt = todo.get("created_at");
        java.sql.Timestamp timestamp;
        if (createdAt instanceof Number) {
            timestamp = new java.sql.Timestamp(((Number) createdAt).longValue());
        } else {
            timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        }
        
        jdbcTemplate.update(sql,
            id,
            todo.get("title"),
            todo.get("description"),
            todo.get("complete"),
            timestamp
        );
        
        return todo;
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Map<String, Object> updateTodo(@PathVariable String id, @RequestBody Map<String, Object> todo) {
        log.info("Updating todo {}: {}", id, todo);
        
        // updated_at 컴럼이 없으므로 제거
        String sql = "UPDATE simple_todo SET title = ?, description = ?, complete = ? WHERE id = ?";
        
        // ID를 integer로 변환
        Integer todoId = Integer.parseInt(id);
        
        jdbcTemplate.update(sql,
            todo.get("title"),
            todo.get("description"),
            todo.get("complete"),
            todoId
        );
        
        todo.put("id", todoId);
        return todo;
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    public void deleteTodo(@PathVariable String id) {
        log.info("Deleting todo: {}", id);
        
        // ID를 integer로 변환
        Integer todoId = Integer.parseInt(id);
        
        String sql = "DELETE FROM simple_todo WHERE id = ?";
        jdbcTemplate.update(sql, todoId);
    }
}