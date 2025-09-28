package point.ttodoApi.todo.application.query;

import java.util.UUID;

public record CalendarQuery(
        UUID userId,
        int year,
        int month
) {
  public static CalendarQuery of(UUID userId, int year, int month) {
    return new CalendarQuery(userId, year, month);
  }
}
