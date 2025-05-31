package point.zzicback.todo.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record TodoListQuery(UUID memberId, Boolean done, Pageable pageable) {
  public static TodoListQuery of(UUID memberId, Boolean done, Pageable pageable) {
    return new TodoListQuery(memberId, done, pageable);
  }
}
