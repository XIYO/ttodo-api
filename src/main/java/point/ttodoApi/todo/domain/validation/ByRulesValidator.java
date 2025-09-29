package point.ttodoApi.todo.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;

import java.util.Set;

public class ByRulesValidator implements ConstraintValidator<ValidByRules, RecurrenceRule> {

    @Override
    public boolean isValid(RecurrenceRule rule, ConstraintValidatorContext context) {
        if (rule == null) {
            return true;
        }

        boolean valid = true;

        if (rule.getByHour() != null && !isValidHours(rule.getByHour())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("byHour는 0-23 범위여야 합니다")
                    .addConstraintViolation();
            valid = false;
        }

        if (rule.getByMinute() != null && !isValidMinutes(rule.getByMinute())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("byMinute는 0-59 범위여야 합니다")
                    .addConstraintViolation();
            valid = false;
        }

        if (rule.getBySecond() != null && !isValidSeconds(rule.getBySecond())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("bySecond는 0-60 범위여야 합니다")
                    .addConstraintViolation();
            valid = false;
        }

        if (rule.getByWeekNo() != null && !isValidWeekNumbers(rule.getByWeekNo())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("byWeekNo는 1-53 또는 -53--1 범위여야 합니다")
                    .addConstraintViolation();
            valid = false;
        }

        if (rule.getByYearDay() != null && !isValidYearDays(rule.getByYearDay())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("byYearDay는 1-366 또는 -366--1 범위여야 합니다")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }

    private boolean isValidHours(Set<Integer> hours) {
        return hours.stream().allMatch(h -> h >= 0 && h <= 23);
    }

    private boolean isValidMinutes(Set<Integer> minutes) {
        return minutes.stream().allMatch(m -> m >= 0 && m <= 59);
    }

    private boolean isValidSeconds(Set<Integer> seconds) {
        return seconds.stream().allMatch(s -> s >= 0 && s <= 60);
    }

    private boolean isValidWeekNumbers(Set<Integer> weekNos) {
        return weekNos.stream().allMatch(w -> (w >= 1 && w <= 53) || (w >= -53 && w <= -1));
    }

    private boolean isValidYearDays(Set<Integer> yearDays) {
        return yearDays.stream().allMatch(d -> (d >= 1 && d <= 366) || (d >= -366 && d <= -1));
    }
}