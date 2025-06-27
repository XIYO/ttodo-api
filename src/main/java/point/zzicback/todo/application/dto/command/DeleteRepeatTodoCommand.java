package point.zzicback.todo.application.dto.command;

import java.util.UUID;

public record DeleteRepeatTodoCommand(
    UUID memberId,
    Long originalTodoId,
    Long daysDifference
) {}
