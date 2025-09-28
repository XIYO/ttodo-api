package point.ttodoApi.profile.domain.validation.optional;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 선택적 프로필 닉네임 검증 어노테이션
 * 선택적이지만 있을 경우 길이 제한 적용
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(min = 1, max = 50, message = "프로필 닉네임은 1-50자 사이여야 합니다")
@Documented
public @interface OptionalProfileNickname {
    String message() default "유효하지 않은 프로필 닉네임입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}