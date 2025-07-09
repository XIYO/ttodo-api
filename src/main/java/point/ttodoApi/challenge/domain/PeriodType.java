package point.ttodoApi.challenge.domain;

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
    
    public PeriodRange calculatePeriod(LocalDate currentDate) {
        return switch (this) {
            case DAILY -> new PeriodRange(currentDate, currentDate);
            case WEEKLY -> {
                LocalDate weekStart = currentDate.with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                yield new PeriodRange(weekStart, weekEnd);
            }
            case MONTHLY -> {
                LocalDate monthStart = currentDate.withDayOfMonth(1);
                LocalDate monthEnd = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
                yield new PeriodRange(monthStart, monthEnd);
            }
        };
    }
    
    public record PeriodRange(LocalDate startDate, LocalDate endDate) {}
}
