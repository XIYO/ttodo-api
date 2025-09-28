package point.ttodoApi.challenge.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 챌린지 날짜 검증 어노테이션
 * 시작일과 종료일 필수 입력 및 유효성 검증
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidChallengeDates {
    String message() default "유효하지 않은 챌린지 날짜입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}