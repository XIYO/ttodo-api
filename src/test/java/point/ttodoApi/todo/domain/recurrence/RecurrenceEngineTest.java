package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecurrenceEngine 반복 생성 테스트")
class RecurrenceEngineTest {

    @Test
    @DisplayName("시간 기반 빈도로 날짜 생성 성공 - HOURLY")
    void generateBetween_ReturnsDate_WithTimeBasedFrequency() {
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
    @DisplayName("BYWEEKNO 필터링 성공 - 1주차만 포함")
    void generateBetween_FiltersCorrectly_WithByWeekNo() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByWeekNo(Set.of(1));
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
    @DisplayName("BYYEARDAY 필터링 성공 - 양수 인덱스 (1일, 365일)")
    void generateBetween_FiltersCorrectly_WithPositiveByYearDay() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByYearDay(Set.of(1, 365));
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        assertEquals(2, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 1, 1)));
        assertTrue(dates.contains(LocalDate.of(2025, 12, 31)));
    }

    @Test
    @DisplayName("BYYEARDAY 필터링 성공 - 음수 인덱스 (마지막 날)")
    void generateBetween_FiltersCorrectly_WithNegativeByYearDay() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByYearDay(Set.of(-1));
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));

        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        assertEquals(1, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 12, 31)));
    }
}