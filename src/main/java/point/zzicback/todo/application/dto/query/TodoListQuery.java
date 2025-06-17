package point.zzicback.todo.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record TodoListQuery(UUID memberId, Boolean done, Integer status, Long categoryId, Integer priority, String keyword, Pageable pageable) {
  public static TodoListQuery of(UUID memberId, Boolean done, Pageable pageable) {
    return new TodoListQuery(memberId, done, null, null, null, null, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, Integer status, Pageable pageable) {
    return new TodoListQuery(memberId, null, status, null, null, null, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, Integer status, String keyword, Pageable pageable) {
    return new TodoListQuery(memberId, null, status, null, null, keyword, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, String keyword, Pageable pageable) {
    return new TodoListQuery(memberId, null, null, null, null, keyword, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, Integer status, Long categoryId, Integer priority, String keyword, Pageable pageable) {
    return new TodoListQuery(memberId, null, status, categoryId, priority, keyword, pageable);
  }
  
  public static TodoListQuery ofAll(UUID memberId, Pageable pageable) {
    return new TodoListQuery(memberId, null, null, null, null, null, pageable);
  }
}
