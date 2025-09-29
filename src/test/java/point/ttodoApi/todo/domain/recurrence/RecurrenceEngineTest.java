package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceEngineTest {

    @Test
    void testTimeBasedFrequenciesReturnDates() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.HOURLY);
        rule.setInterval(1);
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 3);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        assertFalse(dates.isEmpty());
        assertTrue(dates.contains(LocalDate.of(2025, 1, 1)));
    }

    @Test
    void testByWeekNoFiltering() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByWeekNo(Set.of(1)); // First week of year
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 15);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // All generated dates should be in week 1 of 2025
        for (LocalDate date : dates) {
            assertEquals(1, date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        }
    }

    @Test
    void testByYearDayFiltering() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByYearDay(Set.of(1, 365)); // First and last day of year
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // Should only contain Jan 1 and Dec 31
        assertEquals(2, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 1, 1)));
        assertTrue(dates.contains(LocalDate.of(2025, 12, 31)));
    }

    @Test
    void testByYearDayNegativeFiltering() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByYearDay(Set.of(-1)); // Last day of year
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // Should only contain Dec 31 (last day of year)
        assertEquals(1, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 12, 31)));
    }
}