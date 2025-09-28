package point.ttodoApi.sync.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.sync.presentation.dto.request.SyncRequest;
import point.ttodoApi.sync.presentation.dto.response.SyncResponse;
import point.ttodoApi.sync.application.SyncService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SyncController {

    private final SyncService syncService;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/simple-todo")
    public ResponseEntity<SyncResponse> syncSimpleTodos(
            @Valid @RequestBody SyncRequest request) {
        log.info("Sync request received with {} changes", request.getChanges().size());
        
        SyncResponse response = syncService.processSync(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/simple-todos")
    public ResponseEntity<List<Map<String, Object>>> getSimpleTodos() {
        log.info("Fetching all simple todos from PostgreSQL");
        String sql = "SELECT id::text as id, title, description, complete, " +
                    "EXTRACT(EPOCH FROM created_at) * 1000 as created_at " +
                    "FROM simple_todo ORDER BY id";
        List<Map<String, Object>> todos = jdbcTemplate.queryForList(sql);
        log.info("Returning {} todos", todos.size());
        return ResponseEntity.ok(todos);
    }
}