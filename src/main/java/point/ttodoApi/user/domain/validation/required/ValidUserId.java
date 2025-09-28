package point.ttodoApi.user.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 회원 ID 검증 어노테이션
 * UUID 형식 검증
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidUserIdValidator.class})
@Documented
public @interface ValidUserId {
    String message() default "유효하지 않은 회원 ID입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}