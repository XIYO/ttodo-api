package point.ttodoApi.todo.application.command;

import java.util.UUID;

public record DeleteRepeatTodoCommand(
        UUID userId,
        Long originalTodoId,
        Long daysDifference
) {
}
