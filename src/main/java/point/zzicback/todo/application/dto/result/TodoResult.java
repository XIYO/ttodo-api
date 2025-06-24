package point.zzicback.todo.application.dto.result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record TodoResult(
        Long id, 
        String title, 
        String description, 
        Integer statusId,
        String statusName,
        Integer priorityId,
        String priorityName,
        Long categoryId,
        String categoryName,
        LocalDate dueDate,
        LocalTime dueTime,
        Integer repeatType,
        Integer repeatInterval,
        LocalDate repeatEndDate,
        Set<Integer> daysOfWeek,
        Long originalTodoId,
        Set<String> tags
) {
}
