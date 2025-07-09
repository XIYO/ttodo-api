package point.ttodoApi.todo.application.dto.command;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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
        Integer repeatType,
        Integer repeatInterval,
        LocalDate repeatStartDate,
        LocalDate repeatEndDate,
        Set<Integer> daysOfWeek,
        Set<String> tags,
        Long originalTodoId
) {
    public UpdateTodoCommand(UUID memberId, Long todoId, String title, String description, 
                           Boolean complete, Integer priorityId, UUID categoryId, 
                           LocalDate date, LocalTime time, Integer repeatType, 
                           Integer repeatInterval, LocalDate repeatEndDate, Set<String> tags) {
        this(memberId, todoId, title, description, complete, priorityId, categoryId, 
             date, time, repeatType, repeatInterval, null, repeatEndDate, null, tags, null);
    }
    
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
