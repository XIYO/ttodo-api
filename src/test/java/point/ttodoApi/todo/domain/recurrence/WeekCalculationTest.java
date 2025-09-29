package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeekCalculationTest {

    @Test
    void testISOWeekCalculationFor2025() {
        // 2025 starts on Wednesday
        LocalDate jan1 = LocalDate.of(2025, 1, 1);
        LocalDate jan6 = LocalDate.of(2025, 1, 6);
        
        int week1 = jan1.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int week6 = jan6.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        
        System.out.println("Jan 1: week " + week1 + ", Jan 6: week " + week6);
        
        assertEquals(1, week1);
        assertEquals(2, week6); // Monday is in week 2
    }

    @Test
    void testWeeklyWithCorrectByWeekNo() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.WEEKLY);
        rule.setInterval(1);
        rule.setByWeekDays(Set.of(WeekDay.MO));
        rule.setByWeekNo(Set.of(2)); // Second week of year
        rule.setAnchorDate(LocalDate.of(2025, 1, 6)); // Monday in week 2

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        System.out.println("Generated dates: " + dates);
        
        // Should include Monday from week 2 only
        assertTrue(dates.contains(LocalDate.of(2025, 1, 6))); // Week 2 Monday
        assertFalse(dates.contains(LocalDate.of(2025, 1, 13))); // Week 3 Monday  
    }
}