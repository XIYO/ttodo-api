package point.ttodoApi.profile.domain.validation.optional;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 선택적 프로필 자기소개 검증 어노테이션
 * 선택적이지만 있을 경우 길이 제한 적용
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 500, message = "자기소개는 500자 이하여야 합니다")
@Documented
public @interface OptionalProfileIntroduction {
    String message() default "유효하지 않은 자기소개입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}