package point.ttodoApi.todo.application.dto.command;

import java.util.UUID;

public record DeleteTodoCommand(
        UUID memberId,
        Long originalTodoId,
        Long daysDifference,
        Boolean deleteAll
) {}
