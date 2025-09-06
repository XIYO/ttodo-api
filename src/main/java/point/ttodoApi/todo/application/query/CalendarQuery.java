package point.ttodoApi.todo.application.query;

import java.util.UUID;

public record CalendarQuery(
        UUID memberId,
        int year,
        int month
) {
  public static CalendarQuery of(UUID memberId, int year, int month) {
    return new CalendarQuery(memberId, year, month);
  }
}
