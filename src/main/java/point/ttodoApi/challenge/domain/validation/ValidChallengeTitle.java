package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

/**
 * 챌린지 제목 유효성 검증 애노테이션
 * - 필수값 (NotBlank)
 * - 1자 이상 100자 이하
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = TITLE_REQUIRED_MESSAGE)
@Size(min = TITLE_MIN_LENGTH, max = TITLE_MAX_LENGTH, message = "챌린지 제목은 " + TITLE_MIN_LENGTH + "자 이상 " + TITLE_MAX_LENGTH + "자 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidChallengeTitle {
    String message() default "올바른 챌린지 제목 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}