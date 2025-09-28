package point.ttodoApi.todo.application.command;

import java.util.UUID;

public record DeleteTodoCommand(
        UUID userId,
        Long originalTodoId,
        Long daysDifference,
        Boolean deleteAll
) {
}
