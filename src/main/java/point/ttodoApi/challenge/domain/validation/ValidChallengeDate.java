package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.*;
import java.time.LocalDate;

/**
 * 챌린지 날짜 유효성 검증 애노테이션
 * - 미래 날짜만 허용 (오늘 포함)
 * - null 허용하지 않음
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidChallengeDate.ChallengeDateValidator.class)
public @interface ValidChallengeDate {
    String message() default "챌린지 날짜는 오늘 이후여야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class ChallengeDateValidator implements ConstraintValidator<ValidChallengeDate, LocalDate> {
        @Override
        public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
            if (value == null) {
                return false;
            }
            
            return !value.isBefore(LocalDate.now());
        }
    }
}