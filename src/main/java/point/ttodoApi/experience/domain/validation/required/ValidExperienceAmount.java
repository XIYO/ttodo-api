package point.ttodoApi.experience.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 경험치 수량 검증 어노테이션
 * 양수 값 검증
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Positive(message = "경험치는 양수여야 합니다")
@Documented
public @interface ValidExperienceAmount {
    String message() default "유효하지 않은 경험치입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}