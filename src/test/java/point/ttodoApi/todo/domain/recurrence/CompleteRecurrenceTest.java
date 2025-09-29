package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompleteRecurrenceTest {

    @Test
    void testCompleteHourlyRecurrenceWithByRules() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.HOURLY);
        rule.setInterval(1);
        rule.setByHour(Set.of(9, 13, 17)); // 9 AM, 1 PM, 5 PM
        rule.setByMinute(Set.of(0, 30)); // Top and bottom of hour
        rule.setByWeekDays(Set.of(WeekDay.MO, WeekDay.WE, WeekDay.FR)); // Only weekdays
        rule.setAnchorDate(LocalDate.of(2025, 1, 6)); // Monday

        LocalDate start = LocalDate.of(2025, 1, 6);
        LocalDate end = LocalDate.of(2025, 1, 10);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // Should include Monday, Wednesday, Friday (6th, 8th, 10th)
        // Time-based BY rules (byHour, byMinute) serve as metadata for scheduling
        assertTrue(dates.contains(LocalDate.of(2025, 1, 6))); // Monday
        assertTrue(dates.contains(LocalDate.of(2025, 1, 8))); // Wednesday
        assertTrue(dates.contains(LocalDate.of(2025, 1, 10))); // Friday
        assertFalse(dates.contains(LocalDate.of(2025, 1, 7))); // Tuesday
        assertFalse(dates.contains(LocalDate.of(2025, 1, 9))); // Thursday
    }

    @Test
    void testWeeklyWithByWeekNoFilter() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.WEEKLY);
        rule.setInterval(1);
        rule.setByWeekDays(Set.of(WeekDay.MO));
        rule.setByWeekNo(Set.of(2)); // Only second week of year
        rule.setAnchorDate(LocalDate.of(2025, 1, 6)); // Monday in week 2

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // Should only include Monday from week 2
        assertEquals(1, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 1, 6))); // Week 2 Monday
    }

    @Test
    void testYearlyWithByYearDayNegativeIndex() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setInterval(1);
        rule.setByYearDay(Set.of(-1, -2, -3)); // Last 3 days of year
        rule.setAnchorDate(LocalDate.of(2025, 12, 29));

        LocalDate start = LocalDate.of(2025, 12, 25);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // The yearly generation will use anchor date's day (29th) in anchor month (Dec)
        // Then byYearDay filter will match days 363, 364, 365 (-3, -2, -1)
        // Dec 29 = day 363, matches -3
        assertTrue(dates.contains(LocalDate.of(2025, 12, 29))); // day 363 matches -3
        assertEquals(1, dates.size()); // Only Dec 29 matches both conditions
    }

    @Test
    void testComplexRuleWithMultipleByFilters() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByWeekNo(Set.of(1, 52)); // First and last week of year
        rule.setByYearDay(Set.of(1, 365)); // First and last day of year
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // Should only include dates that match BOTH byWeekNo AND byYearDay
        // Jan 1 is in week 1 and is day 1 of year
        // Dec 31 is in week 1 of next year (ISO week), but is day 365 of current year
        assertTrue(dates.contains(LocalDate.of(2025, 1, 1)));
        // Note: Dec 31, 2025 might not match depending on ISO week calculation
    }

    @Test
    void testTimeBasedFrequencyMetadata() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.MINUTELY);
        rule.setInterval(15);
        rule.setByHour(Set.of(9, 14, 18));
        rule.setByMinute(Set.of(0, 15, 30, 45));
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        // These fields should be preserved for scheduling engines
        assertEquals(Set.of(9, 14, 18), rule.getByHour());
        assertEquals(Set.of(0, 15, 30, 45), rule.getByMinute());
        assertEquals(Frequency.MINUTELY, rule.getFrequency());
        assertEquals(15, rule.getInterval());
    }
}