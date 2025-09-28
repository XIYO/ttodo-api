package point.ttodoApi.todo.application.command;

import jakarta.validation.constraints.Positive;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;
import point.ttodoApi.todo.domain.validation.optional.OptionalTodoTitle;
import point.ttodoApi.todo.domain.validation.optional.OptionalTodoDescription;

import java.time.*;
import java.util.*;

/**
 * Todo 수정 명령  
 * TTODO 아키텍처 패턴의 Command 객체로 Todo 수정 요청을 나타냅니다.
 */
public record UpdateTodoCommand(
        UUID userId,
        
        @Positive(message = "Original Todo ID는 양수여야 합니다")
        Long originalTodoId,
        
        @Positive(message = "Days difference는 양수여야 합니다")
        Long daysDifference,
        
        @OptionalTodoTitle
        String title,
        
        @OptionalTodoDescription
        String description,
        
        Boolean complete,
        Integer priorityId,
        UUID categoryId,
        LocalDate date,
        LocalTime time,
        Set<String> tags,
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
