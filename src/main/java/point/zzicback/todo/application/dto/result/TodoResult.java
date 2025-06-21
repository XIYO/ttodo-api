package point.zzicback.todo.application.dto.result;

import point.zzicback.todo.domain.RepeatType;

import java.time.Instant;
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
        Instant dueDate,
        RepeatType repeatType,
        Set<String> tags
) {
}
