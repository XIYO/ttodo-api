package point.zzicback.todo.application.dto.result;

import point.zzicback.todo.domain.*;

import java.time.LocalDate;
import java.util.Set;

public record TodoResult(
        Long id, 
        String title, 
        String description, 
        TodoStatus status,
        Priority priority,
        TodoCategory category,
        String customCategory,
        LocalDate dueDate,
        RepeatType repeatType,
        Set<String> tags,
        String displayCategory,
        String displayPriority,
        String displayStatus
) {
}
