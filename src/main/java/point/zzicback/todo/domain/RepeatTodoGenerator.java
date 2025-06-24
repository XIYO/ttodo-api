package point.zzicback.todo.domain;

import java.time.LocalDate;
import java.util.*;

public final class RepeatTodoGenerator {
    
    private RepeatTodoGenerator() {
    }
    
    public static List<LocalDate> generateRepeatDates(
            LocalDate startDate, 
            Integer repeatType, 
            Integer repeatInterval, 
            LocalDate repeatEndDate) {
        
        if (repeatType == null || repeatType == RepeatTypeConstants.NONE) {
            return List.of();
        }
        
        if (repeatEndDate == null || !startDate.isBefore(repeatEndDate)) {
            return List.of();
        }
        
        int interval = repeatInterval != null && repeatInterval > 0 ? repeatInterval : 1;
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(repeatEndDate)) {
            dates.add(currentDate);
            currentDate = getNextDate(currentDate, repeatType, interval);
            
            if (dates.size() > 365) {
                break;
            }
        }
        
        return dates;
    }
    
    private static LocalDate getNextDate(LocalDate current, Integer repeatType, int interval) {
        return switch (repeatType) {
            case RepeatTypeConstants.DAILY -> current.plusDays(interval);
            case RepeatTypeConstants.WEEKLY -> current.plusWeeks(interval);
            case RepeatTypeConstants.MONTHLY -> current.plusMonths(interval);
            case RepeatTypeConstants.YEARLY -> current.plusYears(interval);
            default -> current.plusDays(1);
        };
    }
}
