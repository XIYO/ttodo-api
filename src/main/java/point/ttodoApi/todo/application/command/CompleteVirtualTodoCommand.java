package point.ttodoApi.todo.application.command;

import java.util.UUID;

public record CompleteVirtualTodoCommand(
        UUID userId,
        Long originalTodoId,
        Long daysDifference
) {
}
