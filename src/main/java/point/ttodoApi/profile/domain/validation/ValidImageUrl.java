package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 이미지 URL 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - 최대 500자
 * - HTTP/HTTPS URL 형식
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다")
@Pattern(regexp = "^(https?://.*)?$", message = "올바른 URL 형식이 아닙니다")
@Constraint(validatedBy = {})
public @interface ValidImageUrl {
    String message() default "올바른 이미지 URL이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}