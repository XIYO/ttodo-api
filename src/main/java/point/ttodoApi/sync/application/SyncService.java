package point.ttodoApi.sync.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.sync.presentation.dto.request.SyncRequest;
import point.ttodoApi.sync.presentation.dto.response.SyncResponse;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public SyncResponse processSync(SyncRequest request) {
    List<SyncResponse.SyncResult> results = new ArrayList<>();
    List<SyncResponse.ConflictRecord> conflicts = new ArrayList<>();

    for (SyncRequest.SyncChange change : request.getChanges()) {
      if (!"simple_todo".equals(change.getTableName())) continue; // Only process simple_todo for now

      try {
        SyncResponse.SyncResult result = processChange(change);
        results.add(result);
      } catch (Exception e) {
        log.error("Failed to process sync change: {}", change, e);
        results.add(SyncResponse.SyncResult.builder()
                .localId(change.getRecord().getId())
                .operation(change.getOperation())
                .success(false)
                .error(e.getMessage())
                .build());
      }
    }

    return SyncResponse.builder()
            .success(true)
            .serverTimestamp(System.currentTimeMillis())
            .results(results)
            .conflicts(conflicts)
            .build();
  }

  private SyncResponse.SyncResult processChange(SyncRequest.SyncChange change) {
    SyncRequest.SyncRecord record = change.getRecord();

    switch (change.getOperation().toLowerCase()) {
      case "insert":
        return handleInsert(record);
      case "update":
        return handleUpdate(record);
      case "delete":
        return handleDelete(record);
      default:
        throw new IllegalArgumentException("Unknown operation: " + change.getOperation());
    }
  }

  private SyncResponse.SyncResult handleInsert(SyncRequest.SyncRecord record) {
    // For insert, we'll generate a new ID on server side
    final String sql = "INSERT INTO simple_todo (title, description, complete, created_at) VALUES (?, ?, ?, ?) RETURNING id";

    Integer serverId = jdbcTemplate.queryForObject(sql, Integer.class,
            record.getTitle(),
            record.getDescription(),
            record.getComplete() != null ? record.getComplete() : false,
            record.getCreatedAt() != null ? Timestamp.valueOf(record.getCreatedAt()) : Timestamp.from(Instant.now())
    );

    return SyncResponse.SyncResult.builder()
            .localId(record.getId())
            .serverId(serverId)
            .operation("insert")
            .success(true)
            .build();
  }

  private SyncResponse.SyncResult handleUpdate(SyncRequest.SyncRecord record) {
    // Last-Write-Wins: Check updated_at timestamp
    final String checkSql = "SELECT updated_at FROM simple_todo WHERE id = ?";
    Long serverUpdatedAt = null;

    try {
      serverUpdatedAt = jdbcTemplate.queryForObject(checkSql, Long.class, record.getId());
    } catch (Exception e) {
      // Record doesn't exist, treat as insert
      return handleInsert(record);
    }

    // Only update if client version is newer
    if (record.getUpdatedAt() != null && serverUpdatedAt != null && record.getUpdatedAt() <= serverUpdatedAt)
      return SyncResponse.SyncResult.builder()
              .localId(record.getId())
              .serverId(Integer.valueOf(record.getId()))
              .operation("update")
              .success(false)
              .error("Server version is newer")
              .build();

    final String sql = "UPDATE simple_todo SET title = ?, description = ?, complete = ?, updated_at = ? WHERE id = ?";

    int updated = jdbcTemplate.update(sql,
            record.getTitle(),
            record.getDescription(),
            record.getComplete(),
            record.getUpdatedAt() != null ? record.getUpdatedAt() : System.currentTimeMillis(),
            record.getId()
    );

    return SyncResponse.SyncResult.builder()
            .localId(record.getId())
            .serverId(Integer.valueOf(record.getId()))
            .operation("update")
            .success(updated > 0)
            .error(updated == 0 ? "Record not found" : null)
            .build();
  }

  private SyncResponse.SyncResult handleDelete(SyncRequest.SyncRecord record) {
    final String sql = "DELETE FROM simple_todo WHERE id = ?";

    int deleted = jdbcTemplate.update(sql, record.getId());

    return SyncResponse.SyncResult.builder()
            .localId(record.getId())
            .operation("delete")
            .success(deleted > 0)
            .error(deleted == 0 ? "Record not found" : null)
            .build();
  }
}