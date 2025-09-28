package point.ttodoApi.challenge.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 챌린지 제목 검증 어노테이션
 * 1-100자 제한 및 빈 문자열 방지
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "챌린지 제목은 필수입니다")
@Size(min = 1, max = 100, message = "챌린지 제목은 1-100자 사이여야 합니다")
@Documented
public @interface ValidChallengeTitle {
    String message() default "유효하지 않은 챌린지 제목입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}