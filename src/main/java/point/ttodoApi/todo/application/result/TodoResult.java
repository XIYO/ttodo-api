package point.ttodoApi.todo.application.result;

import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;

import java.time.*;
import java.util.*;

public record TodoResult(
        String id,
        String title,
        String description,
        Boolean complete,
        Boolean isPinned,
        Integer displayOrder,
        Integer priorityId,
        String priorityName,
        UUID categoryId,
        String categoryName,
        LocalDate date,
        LocalTime time,
        RecurrenceRule recurrenceRule,
        LocalDate anchorDate,
        Set<String> tags,
        Long originalTodoId
) {
}
