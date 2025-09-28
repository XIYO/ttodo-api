package point.ttodoApi.experience.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

import static point.ttodoApi.experience.domain.ExperienceConstants.*;

/**
 * 경험치 유효성 검증 애노테이션
 * - 0 이상 20억 이하
 * - 음수 불가
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Min(value = MIN_EXPERIENCE, message = EXPERIENCE_RANGE_MESSAGE)
@Max(value = MAX_EXPERIENCE, message = EXPERIENCE_RANGE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidExperience {
    String message() default "올바른 경험치 범위가 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
