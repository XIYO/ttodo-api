package point.ttodoApi.auth.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 닉네임 검증 어노테이션
 * 복합 검증: NotNull + NotBlank + Size(1-50)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "닉네임은 필수입니다")
@Size(min = 1, max = 50, message = "닉네임은 1-50자 사이여야 합니다")
@Documented
public @interface ValidNickname {
    String message() default "유효하지 않은 닉네임입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}