package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 로케일 유효성 검증 애노테이션
 * - 필수값
 * - 최대 10자
 * - 형식: 언어코드-국가코드 (예: ko-KR, en-US)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "로케일은 필수입니다")
@Size(max = 10, message = "로케일은 10자 이하여야 합니다")
@Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$", message = "로케일 형식이 올바르지 않습니다 (예: ko-KR)")
@Constraint(validatedBy = {})
public @interface ValidLocale {
    String message() default "올바른 로케일 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}