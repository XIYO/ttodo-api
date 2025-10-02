package point.ttodoApi.todo.domain.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import point.ttodoApi.todo.domain.recurrence.Frequency;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ByRulesValidator 검증 테스트")
class ByRulesValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("BYHOUR 검증")
    class ValidateByHour {

        @Test
        @DisplayName("유효한 시간 범위로 성공 (0-23)")
        void validate_Success_WithValidByHour() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.HOURLY);
            rule.setByHour(Set.of(0, 12, 23));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("잘못된 시간 범위로 실패 (24 이상)")
        void validate_Failure_WithInvalidByHour() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.HOURLY);
            rule.setByHour(Set.of(24, 25));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byHour는 0-23 범위여야 합니다")));
        }
    }

    @Nested
    @DisplayName("BYMINUTE 검증")
    class ValidateByMinute {

        @Test
        @DisplayName("유효한 분 범위로 성공 (0-59)")
        void validate_Success_WithValidByMinute() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.MINUTELY);
            rule.setByMinute(Set.of(0, 30, 59));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("잘못된 분 범위로 실패 (60 이상)")
        void validate_Failure_WithInvalidByMinute() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.MINUTELY);
            rule.setByMinute(Set.of(60, 61));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byMinute는 0-59 범위여야 합니다")));
        }
    }

    @Nested
    @DisplayName("BYWEEKNO 검증")
    class ValidateByWeekNo {

        @Test
        @DisplayName("유효한 주 번호로 성공 (1-53, -53--1)")
        void validate_Success_WithValidByWeekNo() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.YEARLY);
            rule.setByWeekNo(Set.of(1, 26, 53, -1, -26, -53));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("잘못된 주 번호로 실패 (0, 54 이상)")
        void validate_Failure_WithInvalidByWeekNo() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.YEARLY);
            rule.setByWeekNo(Set.of(0, 54, -54));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byWeekNo는 1-53 또는 -53--1 범위여야 합니다")));
        }
    }

    @Nested
    @DisplayName("BYYEARDAY 검증")
    class ValidateByYearDay {

        @Test
        @DisplayName("유효한 연간 일자로 성공 (1-366, -366--1)")
        void validate_Success_WithValidByYearDay() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.YEARLY);
            rule.setByYearDay(Set.of(1, 100, 366, -1, -100, -366));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("잘못된 연간 일자로 실패 (0, 367 이상)")
        void validate_Failure_WithInvalidByYearDay() {
            RecurrenceRule rule = new RecurrenceRule();
            rule.setFrequency(Frequency.YEARLY);
            rule.setByYearDay(Set.of(0, 367, -367));

            Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byYearDay는 1-366 또는 -366--1 범위여야 합니다")));
        }
    }
}