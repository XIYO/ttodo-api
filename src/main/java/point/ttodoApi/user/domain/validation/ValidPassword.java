package point.ttodoApi.user.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static point.ttodoApi.user.domain.UserConstants.*;

/**
 * 비밀번호 유효성 검증 애노테이션
 * - 필수값 (NotBlank)
 * - 8자 이상 128자 이하
 * - 영문, 숫자, 특수문자 포함
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = PASSWORD_REQUIRED_MESSAGE)
@Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = PASSWORD_LENGTH_MESSAGE)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {
    String message() default "올바른 비밀번호 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
