package point.ttodoApi.todo.application.query;

import java.util.UUID;

public record TodoQuery(UUID userId, Long todoId) {
  public static TodoQuery of(UUID userId, Long todoId) {
    return new TodoQuery(userId, todoId);
  }
}
