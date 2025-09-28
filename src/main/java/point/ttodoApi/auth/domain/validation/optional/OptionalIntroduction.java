package point.ttodoApi.auth.domain.validation.optional;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 선택적 소개글 검증 어노테이션
 * 선택적 필드지만 있을 경우 길이 제한 적용
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 500, message = "소개글은 500자를 초과할 수 없습니다")
@Documented
public @interface OptionalIntroduction {
    String message() default "유효하지 않은 소개글입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}