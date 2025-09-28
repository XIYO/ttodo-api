package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

/**
 * 최대 참여자 수 유효성 검증 애노테이션
 * - 1명 이상 10,000명 이하
 * - null 허용 (제한 없음)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Min(value = MAX_PARTICIPANTS_MIN, message = "최대 참여자 수는 " + MAX_PARTICIPANTS_MIN + "명 이상이어야 합니다")
@Max(value = MAX_PARTICIPANTS_MAX, message = "최대 참여자 수는 " + MAX_PARTICIPANTS_MAX + "명 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidMaxParticipants {
    String message() default "올바른 최대 참여자 수가 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}