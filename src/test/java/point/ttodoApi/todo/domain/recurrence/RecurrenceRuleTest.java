package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecurrenceRule 도메인 테스트")
class RecurrenceRuleTest {

    @Test
    @DisplayName("새로운 빈도 타입 설정 성공 - HOURLY/MINUTELY/SECONDLY")
    void setFrequency_Success_WithNewFrequencyTypes() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.HOURLY);
        assertEquals(Frequency.HOURLY, rule.getFrequency());

        rule.setFrequency(Frequency.MINUTELY);
        assertEquals(Frequency.MINUTELY, rule.getFrequency());

        rule.setFrequency(Frequency.SECONDLY);
        assertEquals(Frequency.SECONDLY, rule.getFrequency());
    }

    @Test
    @DisplayName("새로운 BY 규칙 필드 설정 성공 - Hour/Minute/Second/WeekNo/YearDay")
    void setByRuleFields_Success_WithNewFields() {
        RecurrenceRule rule = new RecurrenceRule();

        Set<Integer> byHour = Set.of(9, 14, 18);
        rule.setByHour(byHour);
        assertEquals(byHour, rule.getByHour());

        Set<Integer> byMinute = Set.of(0, 15, 30, 45);
        rule.setByMinute(byMinute);
        assertEquals(byMinute, rule.getByMinute());

        Set<Integer> bySecond = Set.of(0, 30);
        rule.setBySecond(bySecond);
        assertEquals(bySecond, rule.getBySecond());

        Set<Integer> byWeekNo = Set.of(1, 26, 52);
        rule.setByWeekNo(byWeekNo);
        assertEquals(byWeekNo, rule.getByWeekNo());

        Set<Integer> byYearDay = Set.of(1, 100, 365);
        rule.setByYearDay(byYearDay);
        assertEquals(byYearDay, rule.getByYearDay());
    }

    @Test
    @DisplayName("생성자로 새 필드 포함한 RecurrenceRule 생성 성공")
    void createRecurrenceRule_Success_WithConstructorAndNewFields() {
        Set<Integer> byHour = Set.of(9, 17);
        Set<Integer> byMinute = Set.of(0, 30);
        
        RecurrenceRule rule = new RecurrenceRule(
            Frequency.HOURLY, 
            2, 
            null, null, null, null,
            byHour, byMinute, null, null, null,
            WeekDay.MO, 
            null, null, null, 
            "Asia/Seoul", 
            null
        );
        
        assertEquals(Frequency.HOURLY, rule.getFrequency());
        assertEquals(byHour, rule.getByHour());
        assertEquals(byMinute, rule.getByMinute());
    }
}