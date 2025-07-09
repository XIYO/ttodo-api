package point.ttodoApi.todo.application.dto.result;

import java.time.LocalDate;

public record CalendarTodoStatus(
    LocalDate date,
    boolean hasTodo
) {}
