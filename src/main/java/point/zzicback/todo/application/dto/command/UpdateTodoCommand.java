package point.zzicback.todo.application.dto.command;

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
        Integer repeatType,
        Integer repeatInterval,
        LocalDate repeatEndDate,
        Set<String> tags,
        Long originalTodoId
) {
    // 기존 방식과의 호환성을 위한 생성자
    public UpdateTodoCommand(UUID memberId, Long todoId, String title, String description, 
                           Integer statusId, Integer priorityId, Long categoryId, 
                           LocalDate dueDate, LocalTime dueTime, Integer repeatType, 
                           Integer repeatInterval, LocalDate repeatEndDate, Set<String> tags) {
        this(memberId, todoId, title, description, statusId, priorityId, categoryId, 
             dueDate, dueTime, repeatType, repeatInterval, repeatEndDate, tags, null);
    }
}
