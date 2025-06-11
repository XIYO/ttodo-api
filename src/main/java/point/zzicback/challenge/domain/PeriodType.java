package point.zzicback.challenge.domain;

import java.time.LocalDate;

import static java.time.DayOfWeek.*;
import static java.time.temporal.TemporalAdjusters.*;

public enum PeriodType {
    DAILY, WEEKLY, MONTHLY;
    
    public LocalDate calculateTargetDate(LocalDate currentDate) {
        return switch (this) {
            case DAILY -> currentDate;
            case WEEKLY -> currentDate.with(previousOrSame(MONDAY));
            case MONTHLY -> currentDate.withDayOfMonth(1);
        };
    }
    
    public PeriodRange calculatePeriod(LocalDate targetDate) {
        return switch (this) {
            case DAILY -> new PeriodRange(targetDate, targetDate);
            case WEEKLY -> {
                LocalDate weekStart = targetDate.with(previousOrSame(MONDAY));
                LocalDate weekEnd = weekStart.with(nextOrSame(SUNDAY));
                yield new PeriodRange(weekStart, weekEnd);
            }
            case MONTHLY -> {
                LocalDate monthStart = targetDate.withDayOfMonth(1);
                LocalDate monthEnd = monthStart.with(lastDayOfMonth());
                yield new PeriodRange(monthStart, monthEnd);
            }
        };
    }
    
    public record PeriodRange(LocalDate startDate, LocalDate endDate) {}
}
