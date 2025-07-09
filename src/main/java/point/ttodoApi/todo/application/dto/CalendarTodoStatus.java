package point.ttodoApi.todo.application.dto;

import java.time.LocalDate;

public record CalendarTodoStatus(
    LocalDate date,
    boolean hasTodo
) {}
