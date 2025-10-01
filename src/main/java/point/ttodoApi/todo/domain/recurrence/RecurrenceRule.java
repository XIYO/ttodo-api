package point.ttodoApi.todo.domain.recurrence;

import java.time.LocalDate;
import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.todo.domain.validation.ValidByRules;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
@ValidByRules
public class RecurrenceRule {
  Frequency frequency;
  Integer interval = 1;
  Set<WeekDay> byWeekDays;
  Set<Integer> byMonthDay;
  Set<Integer> bySetPos;
  Set<Integer> byMonth;
  Set<Integer> byHour;
  Set<Integer> byMinute;
  Set<Integer> bySecond;
  Set<Integer> byWeekNo;
  Set<Integer> byYearDay;
  WeekDay weekStart = WeekDay.MO;
  EndCondition endCondition;
  Set<LocalDate> exDates;
  Set<LocalDate> rDates;
  String timezone;
  LocalDate anchorDate;
}