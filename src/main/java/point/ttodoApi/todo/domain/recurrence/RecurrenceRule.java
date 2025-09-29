package point.ttodoApi.todo.domain.recurrence;

import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenceRule {
  private Frequency frequency;
  private Integer interval = 1;
  private Set<WeekDay> byWeekDays; // WEEKLY 요일 선택
  private Set<Integer> byMonthDay; // MONTHLY 특정 일자
  private Set<Integer> bySetPos;   // MONTHLY n번째 요일(1..5, -1)
  private Set<Integer> byMonth;    // YEARLY 특정 월
  private WeekDay weekStart = WeekDay.MO;
  private EndCondition endCondition; // NEVER | UNTIL | COUNT
  private Set<LocalDate> exDates;  // 제외 날짜
  private Set<LocalDate> rDates;   // 추가 날짜
  private String timezone;         // 사용자의 프로필 TZ와 일치 권장
  private LocalDate anchorDate;    // 시리즈 기준일
}

