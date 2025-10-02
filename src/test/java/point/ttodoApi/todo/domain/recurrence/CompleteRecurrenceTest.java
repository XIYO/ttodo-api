package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("복합 반복 규칙 테스트")
class CompleteRecurrenceTest {

    @Test
    @DisplayName("시간별 반복 with BY 규칙 조합 성공 - HOURLY + BYHOUR/BYMINUTE/BYWEEKDAY")
    void generateRecurrence_Success_WithCompleteHourlyByRules() {
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
    @DisplayName("주간 반복 with BYWEEKNO 필터 성공 - 특정 주차만")
    void generateRecurrence_Success_WithWeeklyByWeekNoFilter() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.WEEKLY);
        rule.setInterval(1);
        rule.setByWeekDays(Set.of(WeekDay.MO));
        rule.setByWeekNo(Set.of(2));
        rule.setAnchorDate(LocalDate.of(2025, 1, 6));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        assertEquals(1, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 1, 6))); // Week 2 Monday
    }

    @Test
    @DisplayName("연간 반복 with BYYEARDAY 음수 인덱스 성공 - 연말 3일")
    void generateRecurrence_Success_WithYearlyNegativeByYearDay() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setInterval(1);
        rule.setByYearDay(Set.of(-1, -2, -3));
        rule.setAnchorDate(LocalDate.of(2025, 12, 29));

        LocalDate start = LocalDate.of(2025, 12, 25);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        assertTrue(dates.contains(LocalDate.of(2025, 12, 29))); // day 363 matches -3
        assertEquals(1, dates.size());
    }

    @Test
    @DisplayName("복합 필터 조합 성공 - BYWEEKNO + BYYEARDAY 동시 적용")
    void generateRecurrence_Success_WithMultipleByFilters() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByWeekNo(Set.of(1, 52));
        rule.setByYearDay(Set.of(1, 365));
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        assertTrue(dates.contains(LocalDate.of(2025, 1, 1)));
    }

    @Test
    @DisplayName("시간 기반 빈도 메타데이터 보존 확인 - MINUTELY")
    void preserveMetadata_Success_WithTimeBasedFrequency() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.MINUTELY);
        rule.setInterval(15);
        rule.setByHour(Set.of(9, 14, 18));
        rule.setByMinute(Set.of(0, 15, 30, 45));
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        assertEquals(Set.of(9, 14, 18), rule.getByHour());
        assertEquals(Set.of(0, 15, 30, 45), rule.getByMinute());
        assertEquals(Frequency.MINUTELY, rule.getFrequency());
        assertEquals(15, rule.getInterval());
    }
}