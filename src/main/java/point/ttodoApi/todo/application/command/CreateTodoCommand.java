package point.ttodoApi.todo.application.command;

import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;
import point.ttodoApi.todo.domain.validation.required.ValidTodoTitle;
import point.ttodoApi.todo.domain.validation.required.ValidTodoDescription;

import java.time.*;
import java.util.*;

/**
 * Todo 생성 명령
 * TTODO 아키텍처 패턴의 Command 객체로 Todo 생성 요청을 나타냅니다.
 */
public record CreateTodoCommand(
        UUID userId,
        
        @ValidTodoTitle 
        String title,
        
        @ValidTodoDescription
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
    // 간단 검증: frequency 필수, interval>=1
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
