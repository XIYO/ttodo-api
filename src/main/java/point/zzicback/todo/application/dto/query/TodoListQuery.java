package point.zzicback.todo.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

import java.util.List;

public record TodoListQuery(UUID memberId, Boolean done, Integer statusId, Long categoryId,
                            Integer priorityId, String keyword, List<Integer> hideStatusIds,
                            Pageable pageable) {
  public static TodoListQuery of(UUID memberId, Boolean done, Pageable pageable) {
    return new TodoListQuery(memberId, done, null, null, null, null, null, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, Integer statusId, Pageable pageable) {
    return new TodoListQuery(memberId, null, statusId, null, null, null, null, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, Integer statusId, String keyword, Pageable pageable) {
    return new TodoListQuery(memberId, null, statusId, null, null, keyword, null, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, String keyword, Pageable pageable) {
    return new TodoListQuery(memberId, null, null, null, null, keyword, null, pageable);
  }
  
  public static TodoListQuery of(UUID memberId, Integer statusId, Long categoryId, Integer priorityId, String keyword, Pageable pageable) {
    return new TodoListQuery(memberId, null, statusId, categoryId, priorityId, keyword, null, pageable);
  }

  public static TodoListQuery of(UUID memberId, Integer statusId, Long categoryId,
                                 Integer priorityId, String keyword, List<Integer> hideStatusIds,
                                 Pageable pageable) {
    return new TodoListQuery(memberId, null, statusId, categoryId, priorityId, keyword, hideStatusIds, pageable);
  }
  
  public static TodoListQuery ofAll(UUID memberId, Pageable pageable) {
    return new TodoListQuery(memberId, null, null, null, null, null, null, pageable);
  }
}
