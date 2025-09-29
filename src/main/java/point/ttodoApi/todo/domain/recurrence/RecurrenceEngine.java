package point.ttodoApi.todo.domain.recurrence;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.Collectors;

public final class RecurrenceEngine {
  private RecurrenceEngine() {
  }

  public static List<LocalDate> generateBetween(RecurrenceRule rule, LocalDate windowStart, LocalDate windowEnd) {
    if (rule == null || rule.getFrequency() == null || rule.getInterval() == null || rule.getInterval() < 1) {
      return List.of();
    }
    if (windowStart == null || windowEnd == null || windowEnd.isBefore(windowStart)) {
      return List.of();
    }
    LocalDate anchor = rule.getAnchorDate();
    if (anchor == null) {
      // anchor가 없으면 생성 불가
      return List.of();
    }

    // UNTIL 적용
    LocalDate until = null;
    Integer count = null;
    if (rule.getEndCondition() != null) {
      switch (rule.getEndCondition().getType()) {
        case UNTIL -> until = rule.getEndCondition().getUntil();
        case COUNT -> count = rule.getEndCondition().getCount();
        case NEVER -> {
        }
      }
    }

    LocalDate effectiveEnd = windowEnd;
    if (until != null && until.isBefore(effectiveEnd)) {
      effectiveEnd = until;
    }
    if (effectiveEnd.isBefore(windowStart)) {
      return List.of();
    }

    List<LocalDate> dates = switch (rule.getFrequency()) {
      case SECONDLY, MINUTELY, HOURLY -> generateTimeBased(rule, anchor, windowStart, effectiveEnd);
      case DAILY -> generateDaily(rule, anchor, windowStart, effectiveEnd);
      case WEEKLY -> generateWeekly(rule, anchor, windowStart, effectiveEnd);
      case MONTHLY -> generateMonthly(rule, anchor, windowStart, effectiveEnd);
      case YEARLY -> generateYearly(rule, anchor, windowStart, effectiveEnd);
    };

    // COUNT 제한 적용(Anchor 이후 순서로 정렬 후 제한)
    dates = dates.stream().sorted().collect(Collectors.toList());
    if (count != null) {
      dates = dates.stream()
              .filter(d -> !d.isBefore(anchor))
              .limit(count)
              .collect(Collectors.toList());
    }

    // exDates 제외, rDates 추가
    if (rule.getExDates() != null && !rule.getExDates().isEmpty()) {
      dates = dates.stream()
              .filter(d -> !rule.getExDates().contains(d))
              .collect(Collectors.toList());
    }
    if (rule.getRDates() != null && !rule.getRDates().isEmpty()) {
      for (LocalDate extra : rule.getRDates()) {
        if ((extra.isAfter(windowStart) || extra.isEqual(windowStart)) &&
                (extra.isBefore(effectiveEnd) || extra.isEqual(effectiveEnd))) {
          dates.add(extra);
        }
      }
      dates = dates.stream().distinct().sorted().collect(Collectors.toList());
    }

    return dates;
  }

  private static List<LocalDate> generateTimeBased(RecurrenceRule rule, LocalDate anchor, LocalDate start, LocalDate end) {
    // For time-based frequencies, we apply BY rules to determine which dates should be included
    // The actual time components are handled by the UI/frontend for scheduling
    List<LocalDate> baseDates = generateDaily(rule, anchor, start, end);
    
    // Apply BY rules to filter dates
    return applyByRules(baseDates, rule);
  }

  private static List<LocalDate> applyByRules(List<LocalDate> dates, RecurrenceRule rule) {
    return dates.stream()
            .filter(date -> matchesByRules(date, rule))
            .collect(Collectors.toList());
  }

  private static boolean matchesByRules(LocalDate date, RecurrenceRule rule) {
    // Apply BYWEEKNO filter
    if (rule.getByWeekNo() != null && !rule.getByWeekNo().isEmpty()) {
      int weekOfYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
      int maxWeek = date.get(IsoFields.WEEK_BASED_YEAR) == date.getYear() ? 
        date.with(TemporalAdjusters.lastDayOfYear()).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) : 53;
      
      boolean matches = false;
      for (Integer weekNo : rule.getByWeekNo()) {
        if (weekNo > 0 && weekNo == weekOfYear) {
          matches = true;
          break;
        } else if (weekNo < 0 && (maxWeek + weekNo + 1) == weekOfYear) {
          matches = true;
          break;
        }
      }
      if (!matches) return false;
    }

    // Apply BYYEARDAY filter
    if (rule.getByYearDay() != null && !rule.getByYearDay().isEmpty()) {
      int dayOfYear = date.getDayOfYear();
      int yearLength = Year.of(date.getYear()).length();
      
      boolean matches = false;
      for (Integer yearDay : rule.getByYearDay()) {
        if (yearDay > 0 && yearDay == dayOfYear) {
          matches = true;
          break;
        } else if (yearDay < 0 && (yearLength + yearDay + 1) == dayOfYear) {
          matches = true;
          break;
        }
      }
      if (!matches) return false;
    }

    return true;
  }

  private static List<LocalDate> generateDaily(RecurrenceRule rule, LocalDate anchor, LocalDate start, LocalDate end) {
    List<LocalDate> out = new ArrayList<>();
    int interval = Math.max(1, rule.getInterval());

    LocalDate first = start.isBefore(anchor) ? anchor : start;
    long offset = Duration.between(anchor.atStartOfDay(), first.atStartOfDay()).toDays();
    long mod = ((offset % interval) + interval) % interval;
    if (mod != 0) {
      first = first.plusDays(interval - mod);
    }
    for (LocalDate d = first; !d.isAfter(end); d = d.plusDays(interval)) {
      if (matchesByRules(d, rule)) {
        out.add(d);
      }
    }
    return out;
  }

  private static List<LocalDate> generateWeekly(RecurrenceRule rule, LocalDate anchor, LocalDate start, LocalDate end) {
    List<LocalDate> out = new ArrayList<>();
    int interval = Math.max(1, rule.getInterval());
    Set<WeekDay> byDays = rule.getByWeekDays();
    if (byDays == null || byDays.isEmpty()) {
      // anchor의 요일로 반복
      byDays = EnumSet.of(fromJavaDay(anchor.getDayOfWeek()));
    }
    WeekDay weekStart = rule.getWeekStart() != null ? rule.getWeekStart() : WeekDay.MO;
    LocalDate anchorWeekStart = toWeekStart(anchor, weekStart);

    for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
      LocalDate curWeekStart = toWeekStart(d, weekStart);
      long weeks = Duration.between(anchorWeekStart.atStartOfDay(), curWeekStart.atStartOfDay()).toDays() / 7;
      if (weeks >= 0 && weeks % interval == 0) {
        if (byDays.contains(fromJavaDay(d.getDayOfWeek()))) {
          if (!d.isBefore(anchor) && matchesByRules(d, rule)) {
            out.add(d);
          }
        }
      }
    }
    return applyByRules(out, rule);
  }

  private static List<LocalDate> generateMonthly(RecurrenceRule rule, LocalDate anchor, LocalDate start, LocalDate end) {
    List<LocalDate> out = new ArrayList<>();
    int interval = Math.max(1, rule.getInterval());
    Set<Integer> byMonthDay = rule.getByMonthDay();
    Set<Integer> bySetPos = rule.getBySetPos();
    Set<WeekDay> byDays = rule.getByWeekDays();

    YearMonth anchorYm = YearMonth.from(anchor);

    LocalDate cursor = LocalDate.of(start.getYear(), start.getMonth(), 1);
    while (!cursor.isAfter(end)) {
      YearMonth ym = YearMonth.from(cursor);
      long months = anchorYm.until(ym, ChronoUnit.MONTHS);
      if (months >= 0 && months % interval == 0) {
        if (byMonthDay != null && !byMonthDay.isEmpty()) {
          for (Integer md : byMonthDay) {
            if (md >= 1 && md <= ym.lengthOfMonth()) {
              LocalDate d = ym.atDay(md);
              if (!d.isBefore(start) && !d.isAfter(end) && !d.isBefore(anchor)) {
                out.add(d);
              }
            }
          }
        }
        if (bySetPos != null && !bySetPos.isEmpty() && byDays != null && !byDays.isEmpty()) {
          for (WeekDay wd : byDays) {
            List<LocalDate> candidates = allWeekdayDatesOfMonth(ym, wd);
            for (Integer pos : bySetPos) {
              LocalDate d = pickBySetPos(candidates, pos);
              if (d != null && !d.isBefore(start) && !d.isAfter(end) && !d.isBefore(anchor)) {
                out.add(d);
              }
            }
          }
        }
      }
      cursor = cursor.plusMonths(1);
    }
    return applyByRules(out, rule);
  }

  private static List<LocalDate> generateYearly(RecurrenceRule rule, LocalDate anchor, LocalDate start, LocalDate end) {
    List<LocalDate> out = new ArrayList<>();
    int interval = Math.max(1, rule.getInterval());
    Set<Integer> byMonth = rule.getByMonth();
    Set<Integer> byMonthDay = rule.getByMonthDay();
    Set<Integer> bySetPos = rule.getBySetPos();
    Set<WeekDay> byDays = rule.getByWeekDays();

    int anchorYear = anchor.getYear();
    for (int y = start.getYear(); y <= end.getYear(); y++) {
      int years = y - anchorYear;
      if (years < 0 || years % interval != 0) continue;
      Set<Integer> months = (byMonth == null || byMonth.isEmpty()) ? Set.of(anchor.getMonthValue()) : byMonth;
      for (Integer m : months) {
        if (m < 1 || m > 12) continue;
        YearMonth ym = YearMonth.of(y, m);
        if (byMonthDay != null && !byMonthDay.isEmpty()) {
          for (Integer md : byMonthDay) {
            if (md >= 1 && md <= ym.lengthOfMonth()) {
              LocalDate d = ym.atDay(md);
              if (!d.isBefore(start) && !d.isAfter(end) && !d.isBefore(anchor)) {
                out.add(d);
              }
            }
          }
        }
        if (bySetPos != null && !bySetPos.isEmpty() && byDays != null && !byDays.isEmpty()) {
          for (WeekDay wd : byDays) {
            List<LocalDate> candidates = allWeekdayDatesOfMonth(ym, wd);
            for (Integer pos : bySetPos) {
              LocalDate d = pickBySetPos(candidates, pos);
              if (d != null && !d.isBefore(start) && !d.isAfter(end) && !d.isBefore(anchor)) {
                out.add(d);
              }
            }
          }
        }
      }
    }
    return applyByRules(out, rule);
  }

  private static List<LocalDate> allWeekdayDatesOfMonth(YearMonth ym, WeekDay wd) {
    List<LocalDate> list = new ArrayList<>();
    LocalDate d = ym.atDay(1);
    DayOfWeek target = toJavaDay(wd);
    d = d.with(TemporalAdjusters.firstInMonth(target));
    while (YearMonth.from(d).equals(ym)) {
      list.add(d);
      d = d.plusWeeks(1);
    }
    return list;
  }

  private static LocalDate pickBySetPos(List<LocalDate> candidates, Integer pos) {
    if (candidates.isEmpty() || pos == null) return null;
    if (pos == -1) return candidates.getLast();
    if (pos >= 1 && pos <= candidates.size()) return candidates.get(pos - 1);
    return null;
  }

  private static LocalDate toWeekStart(LocalDate d, WeekDay weekStart) {
    DayOfWeek startDow = toJavaDay(weekStart);
    int diff = (7 + (d.getDayOfWeek().getValue() - startDow.getValue())) % 7;
    return d.minusDays(diff);
  }

  private static DayOfWeek toJavaDay(WeekDay wd) {
    return switch (wd) {
      case MO -> DayOfWeek.MONDAY;
      case TU -> DayOfWeek.TUESDAY;
      case WE -> DayOfWeek.WEDNESDAY;
      case TH -> DayOfWeek.THURSDAY;
      case FR -> DayOfWeek.FRIDAY;
      case SA -> DayOfWeek.SATURDAY;
      case SU -> DayOfWeek.SUNDAY;
    };
  }

  private static WeekDay fromJavaDay(DayOfWeek d) {
    return switch (d) {
      case MONDAY -> WeekDay.MO;
      case TUESDAY -> WeekDay.TU;
      case WEDNESDAY -> WeekDay.WE;
      case THURSDAY -> WeekDay.TH;
      case FRIDAY -> WeekDay.FR;
      case SATURDAY -> WeekDay.SA;
      case SUNDAY -> WeekDay.SU;
    };
  }
}

