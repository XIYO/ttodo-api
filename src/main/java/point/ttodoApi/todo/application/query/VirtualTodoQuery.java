package point.ttodoApi.todo.application.query;

import java.util.UUID;

public record VirtualTodoQuery(UUID memberId, Long originalTodoId, Long daysDifference) {
  public static VirtualTodoQuery of(UUID memberId, Long originalTodoId, Long daysDifference) {
    return new VirtualTodoQuery(memberId, originalTodoId, daysDifference);
  }
}
