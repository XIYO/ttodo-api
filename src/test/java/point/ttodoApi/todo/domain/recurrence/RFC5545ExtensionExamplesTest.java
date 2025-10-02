package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 5545 확장 기능 사용 예시 테스트
 */
@DisplayName("RFC5545 확장 기능 예시 테스트")
class RFC5545ExtensionExamplesTest {

    @Test
    @DisplayName("약 복용 알림 예시 - 평일 특정 시간")
    void generateReminder_Success_WithMedicationSchedule() {
        // Take medicine every day at specific times on weekdays
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.HOURLY);
        rule.setInterval(1); // Every hour (filtered by byHour)
        rule.setByHour(Set.of(8, 12, 16, 20)); // 8AM, 12PM, 4PM, 8PM
        rule.setByWeekDays(Set.of(WeekDay.MO, WeekDay.TU, WeekDay.WE, WeekDay.TH, WeekDay.FR));
        rule.setAnchorDate(LocalDate.of(2025, 1, 6)); // Monday

        LocalDate start = LocalDate.of(2025, 1, 6);
        LocalDate end = LocalDate.of(2025, 1, 10);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);
        
        // Should include weekdays only (time info in byHour is metadata)
        assertTrue(dates.contains(LocalDate.of(2025, 1, 6))); // Monday
        assertTrue(dates.contains(LocalDate.of(2025, 1, 7))); // Tuesday
        assertTrue(dates.contains(LocalDate.of(2025, 1, 8))); // Wednesday
        assertTrue(dates.contains(LocalDate.of(2025, 1, 9))); // Thursday
        assertTrue(dates.contains(LocalDate.of(2025, 1, 10))); // Friday
        assertEquals(5, dates.size());
    }

    @Test
    @DisplayName("분기별 보고서 예시 - 분기 말 영업일")
    void generateReport_Success_WithQuarterlySchedule() {
        // Generate quarterly reports on last business day of quarter
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setInterval(1);
        rule.setByMonth(Set.of(3, 6, 9, 12));
        rule.setByWeekDays(Set.of(WeekDay.MO, WeekDay.TU, WeekDay.WE, WeekDay.TH, WeekDay.FR));
        rule.setBySetPos(Set.of(-1));
        rule.setAnchorDate(LocalDate.of(2025, 3, 31));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        assertFalse(dates.isEmpty());
        System.out.println("Quarterly business days: " + dates);
    }

    @Test
    @DisplayName("월별 첫 주 회의 예시 - 매월 첫째 화요일")
    void generateMeeting_Success_WithFirstWeekOfMonth() {
        // Meeting every first week of month on Tuesdays
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.WEEKLY);
        rule.setInterval(1);
        rule.setByWeekDays(Set.of(WeekDay.TU));
        rule.setBySetPos(Set.of(1));
        rule.setAnchorDate(LocalDate.of(2025, 1, 7));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        System.out.println("First Tuesday of each period: " + dates);
        assertFalse(dates.isEmpty());
    }

    @Test
    @DisplayName("연말 작업 예시 - 마지막 주 업무")
    void generateTasks_Success_WithYearEndSchedule() {
        // Tasks in the last week of the year
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(1);
        rule.setByWeekNo(Set.of(-1));
        rule.setAnchorDate(LocalDate.of(2025, 12, 29));

        LocalDate start = LocalDate.of(2025, 12, 20);
        LocalDate end = LocalDate.of(2025, 12, 31);

        List<LocalDate> dates = RecurrenceEngine.generateBetween(rule, start, end);

        System.out.println("Last week of year dates: " + dates);
        assertFalse(dates.isEmpty());
    }
}