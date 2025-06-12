package point.zzicback.challenge.domain;

import java.time.LocalDate;

public enum PeriodType {
    DAILY, WEEKLY, MONTHLY;
    
    public LocalDate calculateTargetDate(LocalDate currentDate) {
        return switch (this) {
            case DAILY -> currentDate;
            case WEEKLY -> currentDate;
            case MONTHLY -> currentDate;
        };
    }
    
    public PeriodRange calculatePeriod(LocalDate targetDate) {
        return switch (this) {
            case DAILY -> new PeriodRange(targetDate, targetDate);
            case WEEKLY -> {
                LocalDate weekEnd = targetDate.plusWeeks(1).minusDays(1);
                yield new PeriodRange(targetDate, weekEnd);
            }
            case MONTHLY -> {
                LocalDate monthEnd = targetDate.plusMonths(1).minusDays(1);
                yield new PeriodRange(targetDate, monthEnd);
            }
        };
    }
    
    public record PeriodRange(LocalDate startDate, LocalDate endDate) {}
}
