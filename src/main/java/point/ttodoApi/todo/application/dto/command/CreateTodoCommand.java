package point.ttodoApi.todo.application.dto.command;

import java.time.*;
import java.util.*;

public record CreateTodoCommand(
        UUID memberId,
        String title,
        String description,
        Boolean complete,
        Integer priorityId,
        UUID categoryId,
        LocalDate date,
        LocalTime time,
        Integer repeatType,
        Integer repeatInterval,
        LocalDate repeatStartDate,
        LocalDate repeatEndDate,
        Set<Integer> daysOfWeek,
        Set<String> tags
) {
    public void validateRepeatDates() {
        if (repeatType != null && repeatType != 0) {
            if (repeatStartDate != null && date != null && repeatStartDate.isBefore(date)) {
                throw new IllegalArgumentException("반복 시작일은 기본 날짜보다 이전일 수 없습니다.");
            }
            
            if (repeatEndDate != null && repeatStartDate != null && repeatEndDate.isBefore(repeatStartDate)) {
                throw new IllegalArgumentException("반복 종료일은 반복 시작일보다 이전일 수 없습니다.");
            }
            
            if (repeatEndDate != null && date != null && repeatEndDate.isBefore(date)) {
                throw new IllegalArgumentException("반복 종료일은 기본 날짜보다 이전일 수 없습니다.");
            }
        }
    }
}
