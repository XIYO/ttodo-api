package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 닉네임 유효성 검증 애노테이션
 * - 필수값 (NotBlank)
 * - 1자 이상 100자 이하
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "닉네임은 필수입니다")
@Size(min = 1, max = 100, message = "닉네임은 1자 이상 100자 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidNickname {
    String message() default "올바른 닉네임 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}