package point.zzicback.todo.application.dto.command;

import point.zzicback.todo.domain.RepeatType;

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
        RepeatType repeatType,
        Set<String> tags
) {
}
