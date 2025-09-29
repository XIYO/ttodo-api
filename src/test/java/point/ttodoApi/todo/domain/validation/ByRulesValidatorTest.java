package point.ttodoApi.todo.domain.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import point.ttodoApi.todo.domain.recurrence.Frequency;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ByRulesValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidByHour() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.HOURLY);
        rule.setByHour(Set.of(0, 12, 23));

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidByHour() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.HOURLY);
        rule.setByHour(Set.of(24, 25)); // Invalid hours

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byHour는 0-23 범위여야 합니다")));
    }

    @Test
    void testValidByMinute() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.MINUTELY);
        rule.setByMinute(Set.of(0, 30, 59));

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidByMinute() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.MINUTELY);
        rule.setByMinute(Set.of(60, 61)); // Invalid minutes

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byMinute는 0-59 범위여야 합니다")));
    }

    @Test
    void testValidByWeekNo() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setByWeekNo(Set.of(1, 26, 53, -1, -26, -53));

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidByWeekNo() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setByWeekNo(Set.of(0, 54, -54)); // Invalid week numbers

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byWeekNo는 1-53 또는 -53--1 범위여야 합니다")));
    }

    @Test
    void testValidByYearDay() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setByYearDay(Set.of(1, 100, 366, -1, -100, -366));

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidByYearDay() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setByYearDay(Set.of(0, 367, -367)); // Invalid year days

        Set<ConstraintViolation<RecurrenceRule>> violations = validator.validate(rule);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("byYearDay는 1-366 또는 -366--1 범위여야 합니다")));
    }
}