package point.ttodoApi.todo.application.dto.command;

import java.util.UUID;

public record CompleteVirtualTodoCommand(
        UUID memberId,
        Long originalTodoId,
        Long daysDifference
) {
}
