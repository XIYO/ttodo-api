package point.zzicback.todo.application.dto.command;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public record CreateTodoCommand(
        UUID memberId,
        String title,
        String description,
        Integer priorityId,
        Long categoryId,
        LocalDate dueDate,
        LocalTime dueTime,
        Integer repeatType,
        Integer repeatInterval,
        LocalDate repeatStartDate,
        LocalDate repeatEndDate,
        Set<String> tags
) {
}
