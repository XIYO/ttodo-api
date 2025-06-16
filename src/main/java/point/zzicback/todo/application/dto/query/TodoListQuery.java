package point.zzicback.todo.application.dto.query;

import org.springframework.data.domain.Pageable;
import point.zzicback.todo.domain.TodoStatus;

import java.util.UUID;

public record TodoListQuery(UUID memberId, Boolean done, TodoStatus status, Pageable pageable) {
  public static TodoListQuery of(UUID memberId, Boolean done, Pageable pageable) {
    return new TodoListQuery(memberId, done, null, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, TodoStatus status, Pageable pageable) {
    return new TodoListQuery(memberId, null, status, pageable);
  }
  
  public static TodoListQuery ofAll(UUID memberId, Pageable pageable) {
    return new TodoListQuery(memberId, null, null, pageable);
  }
}
