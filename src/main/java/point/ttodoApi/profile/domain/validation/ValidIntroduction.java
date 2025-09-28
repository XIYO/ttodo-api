package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 소개글 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - 최대 500자
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = 500, message = "소개글은 500자 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidIntroduction {
    String message() default "올바른 소개글 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}