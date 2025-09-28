package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.*;
import java.time.LocalDate;

/**
 * 챌린지 기간 유효성 검증 애노테이션
 * - 시작일이 종료일보다 빠르거나 같아야 함
 * - 클래스 레벨 검증
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidChallengePeriod.ChallengePeriodValidator.class)
public @interface ValidChallengePeriod {
    String message() default "시작일은 종료일보다 빠르거나 같아야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class ChallengePeriodValidator implements ConstraintValidator<ValidChallengePeriod, Object> {
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            
            try {
                LocalDate startDate = getStartDate(value);
                LocalDate endDate = getEndDate(value);
                
                if (startDate == null || endDate == null) {
                    return true; // 다른 검증기에서 처리
                }
                
                return !startDate.isAfter(endDate);
            } catch (Exception e) {
                return false;
            }
        }
        
        private LocalDate getStartDate(Object obj) {
            try {
                return (LocalDate) obj.getClass().getMethod("getStartDate").invoke(obj);
            } catch (Exception e) {
                return null;
            }
        }
        
        private LocalDate getEndDate(Object obj) {
            try {
                return (LocalDate) obj.getClass().getMethod("getEndDate").invoke(obj);
            } catch (Exception e) {
                return null;
            }
        }
    }
}