package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("주차 계산 테스트")
class WeekCalculationTest {

    @Test
    @DisplayName("ISO 주차 계산 성공 - 2025년 1월")
    void calculateISOWeek_Success_For2025January() {
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
    @DisplayName("주간 반복에서 BYWEEKNO 필터링 성공 - 2주차만 포함")
    void generateWeekly_FiltersCorrectly_WithByWeekNo() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.WEEKLY);
        rule.setInterval(1);
        rule.setByWeekDays(Set.of(WeekDay.MO));
        rule.setByWeekNo(Set.of(2));
        rule.setAnchorDate(LocalDate.of(2025, 1, 6));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        System.out.println("Generated dates: " + dates);

        assertTrue(dates.contains(LocalDate.of(2025, 1, 6))); // Week 2 Monday
        assertFalse(dates.contains(LocalDate.of(2025, 1, 13))); // Week 3 Monday
    }
}