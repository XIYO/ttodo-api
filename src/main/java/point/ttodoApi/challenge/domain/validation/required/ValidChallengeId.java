package point.ttodoApi.challenge.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 챌린지 ID 검증 어노테이션
 * Long 타입 ID 검증
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidChallengeId {
    String message() default "유효하지 않은 챌린지 ID입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}