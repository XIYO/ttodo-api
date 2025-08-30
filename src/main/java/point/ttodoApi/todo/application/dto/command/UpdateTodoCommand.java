package point.ttodoApi.todo.application.dto.command;

import java.time.*;
import java.util.*;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;

public record UpdateTodoCommand(
        UUID memberId,
        Long todoId,
        String title,
        String description,
        Boolean complete,
        Integer priorityId,
        UUID categoryId,
        LocalDate date,
        LocalTime time,
        Set<String> tags,
        Long originalTodoId,
        RecurrenceRule recurrenceRule
) {
    public void validateRule() {
        if (recurrenceRule != null) {
            if (recurrenceRule.getFrequency() == null) {
                throw new IllegalArgumentException("recurrenceRule.frequency is required");
            }
            if (recurrenceRule.getInterval() != null && recurrenceRule.getInterval() < 1) {
                throw new IllegalArgumentException("recurrenceRule.interval must be >= 1");
            }
        }
    }
}
