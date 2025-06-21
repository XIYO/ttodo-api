package point.zzicback.todo.application.dto.command;

import jakarta.validation.constraints.*;
import point.zzicback.todo.domain.RepeatType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UpdateTodoCommand(
        @NotNull UUID memberId, 
        @NotNull Long todoId, 
        @Size(max = 255) String title,
        @Size(max = 1000) String description, 
        Integer statusId,
        Integer priorityId,
        Long categoryId,
        Instant dueDate,
        RepeatType repeatType,
        Set<String> tags
) {
}
