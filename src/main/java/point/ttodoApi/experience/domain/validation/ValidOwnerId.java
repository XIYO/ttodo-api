package point.ttodoApi.experience.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;

import static point.ttodoApi.experience.domain.ExperienceConstants.*;

/**
 * 소유자 ID 유효성 검증 애노테이션
 * - 필수값 (NotNull)
 * - UUID 형식 유효성
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull(message = OWNER_ID_REQUIRED_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidOwnerId {
    String message() default "올바른 소유자 ID 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
