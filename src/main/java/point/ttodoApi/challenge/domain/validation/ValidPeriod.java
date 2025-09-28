package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import point.ttodoApi.challenge.domain.Period;

import java.lang.annotation.*;

/**
 * Period 유효성 검증 애노테이션
 * - 시작일이 종료일보다 빠르거나 같아야 함
 * - null 허용
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidPeriod.PeriodValidator.class)
public @interface ValidPeriod {
    String message() default "기간의 시작일은 종료일보다 빠르거나 같아야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class PeriodValidator implements ConstraintValidator<ValidPeriod, Period> {
        @Override
        public boolean isValid(Period period, ConstraintValidatorContext context) {
            // null은 허용
            if (period == null) {
                return true;
            }
            
            // startDate나 endDate가 null이면 허용 (다른 검증기에서 처리)
            if (period.getStartDate() == null || period.getEndDate() == null) {
                return true;
            }
            
            return !period.getStartDate().isAfter(period.getEndDate());
        }
    }
}