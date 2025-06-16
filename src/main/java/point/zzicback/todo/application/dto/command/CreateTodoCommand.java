package point.zzicback.todo.application.dto.command;

import jakarta.validation.constraints.*;
import point.zzicback.todo.domain.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CreateTodoCommand(
        @NotNull UUID memberId, 
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        Priority priority,
        TodoCategory category,
        String customCategory,
        LocalDate dueDate,
        RepeatType repeatType,
        Set<String> tags
) {
}
