package point.ttodoApi.user.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 회원 이메일 검증 어노테이션
 * 복합 검증: NotNull + NotBlank + Email 형식
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "회원 이메일은 필수입니다")
@Email(message = "유효한 이메일 형식이 아닙니다")
@Documented
public @interface ValidUserEmail {
    String message() default "유효하지 않은 회원 이메일입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}