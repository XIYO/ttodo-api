package point.ttodoApi.todo.domain.recurrence;

import java.time.LocalDate;
import java.util.Set;

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

  public RecurrenceRule() {
  }

  public Frequency getFrequency() {
    return frequency;
  }

  public void setFrequency(Frequency frequency) {
    this.frequency = frequency;
  }

  public Integer getInterval() {
    return interval;
  }

  public void setInterval(Integer interval) {
    this.interval = interval;
  }

  public Set<WeekDay> getByWeekDays() {
    return byWeekDays;
  }

  public void setByWeekDays(Set<WeekDay> byWeekDays) {
    this.byWeekDays = byWeekDays;
  }

  public Set<Integer> getByMonthDay() {
    return byMonthDay;
  }

  public void setByMonthDay(Set<Integer> byMonthDay) {
    this.byMonthDay = byMonthDay;
  }

  public Set<Integer> getBySetPos() {
    return bySetPos;
  }

  public void setBySetPos(Set<Integer> bySetPos) {
    this.bySetPos = bySetPos;
  }

  public Set<Integer> getByMonth() {
    return byMonth;
  }

  public void setByMonth(Set<Integer> byMonth) {
    this.byMonth = byMonth;
  }

  public WeekDay getWeekStart() {
    return weekStart;
  }

  public void setWeekStart(WeekDay weekStart) {
    this.weekStart = weekStart;
  }

  public EndCondition getEndCondition() {
    return endCondition;
  }

  public void setEndCondition(EndCondition endCondition) {
    this.endCondition = endCondition;
  }

  public Set<LocalDate> getExDates() {
    return exDates;
  }

  public void setExDates(Set<LocalDate> exDates) {
    this.exDates = exDates;
  }

  public Set<LocalDate> getRDates() {
    return rDates;
  }

  public void setRDates(Set<LocalDate> rDates) {
    this.rDates = rDates;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public LocalDate getAnchorDate() {
    return anchorDate;
  }

  public void setAnchorDate(LocalDate anchorDate) {
    this.anchorDate = anchorDate;
  }
}

