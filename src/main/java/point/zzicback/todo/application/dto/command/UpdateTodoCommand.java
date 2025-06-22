package point.zzicback.todo.application.dto.command;

import point.zzicback.todo.domain.RepeatType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UpdateTodoCommand(
        UUID memberId,
        Long todoId,
        String title,
        String description,
        Integer statusId,
        Integer priorityId,
        Long categoryId,
        Instant dueDate,
        RepeatType repeatType,
        Set<String> tags
) {
}
