package point.ttodoApi.todo.application.command;

import java.util.UUID;

public record DeleteRepeatTodoCommand(
        UUID memberId,
        Long originalTodoId,
        Long daysDifference
) {
}
