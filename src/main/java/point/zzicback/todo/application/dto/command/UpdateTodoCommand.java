package point.zzicback.todo.application.dto.command;

import point.zzicback.todo.domain.RepeatType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public record UpdateTodoCommand(
        UUID memberId,
        Long todoId,
        String title,
        String description,
        Integer statusId,
        Integer priorityId,
        Long categoryId,
        LocalDate dueDate,
        LocalTime dueTime,
        RepeatType repeatType,
        Set<String> tags
) {
}
