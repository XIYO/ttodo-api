package point.zzicback.todo.application.dto.command;

import point.zzicback.todo.domain.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateTodoCommand(
        UUID memberId,
        String title,
        String description,
        Integer priorityId,
        Long categoryId,
        Instant dueDate,
        RepeatType repeatType,
        Set<String> tags
) {
}
