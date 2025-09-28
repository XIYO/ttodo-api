package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

/**
 * 챌린지 설명 유효성 검증 애노테이션
 * - 선택사항
 * - 5000자 이하
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = DESCRIPTION_MAX_LENGTH, message = "챌린지 설명은 " + DESCRIPTION_MAX_LENGTH + "자 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidChallengeDescription {
    String message() default "올바른 챌린지 설명 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}