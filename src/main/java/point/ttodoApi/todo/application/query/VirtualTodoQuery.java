package point.ttodoApi.todo.application.query;

import java.util.UUID;

public record VirtualTodoQuery(UUID userId, Long originalTodoId, Long daysDifference) {
  public static VirtualTodoQuery of(UUID userId, Long originalTodoId, Long daysDifference) {
    return new VirtualTodoQuery(userId, originalTodoId, daysDifference);
  }
}
