package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

/**
 * 리더 제거 사유 유효성 검증 애노테이션
 * - 선택사항
 * - 500자 이하
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = REMOVAL_REASON_MAX_LENGTH, message = "제거 사유는 " + REMOVAL_REASON_MAX_LENGTH + "자 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidRemovalReason {
    String message() default "올바른 제거 사유 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}