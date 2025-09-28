package point.ttodoApi.experience.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

import static point.ttodoApi.experience.domain.ExperienceConstants.*;

/**
 * 경험치 증감 유효성 검증 애노테이션
 * - 1 이상 10,000 이하
 * - 경험치 증감 메소드에 사용
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Min(value = EXPERIENCE_INCREMENT_MIN, message = EXPERIENCE_INCREMENT_MESSAGE)
@Max(value = EXPERIENCE_INCREMENT_MAX, message = EXPERIENCE_INCREMENT_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidExperienceIncrement {
    String message() default "올바른 경험치 증감량이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
