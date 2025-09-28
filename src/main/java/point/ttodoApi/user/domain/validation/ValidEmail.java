package point.ttodoApi.user.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static point.ttodoApi.user.domain.UserConstants.*;

/**
 * 이메일 유효성 검증 애노테이션
 * - 필수값 (NotBlank)
 * - 이메일 형식 검증
 * - 최대 320자 (RFC 5321 표준)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = EMAIL_REQUIRED_MESSAGE)
@Email(message = EMAIL_FORMAT_MESSAGE)
@Size(max = EMAIL_MAX_LENGTH, message = "이메일은 " + EMAIL_MAX_LENGTH + "자 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidEmail {
    String message() default "올바른 이메일 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
