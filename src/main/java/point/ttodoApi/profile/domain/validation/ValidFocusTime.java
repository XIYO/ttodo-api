package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

import static point.ttodoApi.profile.domain.StatisticsConstants.*;

/**
 * 총 집중 시간 유효성 검증 애노테이션
 * - 0 이상 157,680,000초(5년) 이하
 * - 초 단위
 * - null 허용 (기본값 0으로 처리)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Min(value = MIN_FOCUS_TIME, message = FOCUS_TIME_MESSAGE)
@Max(value = MAX_FOCUS_TIME, message = FOCUS_TIME_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidFocusTime {
    String message() default "올바른 집중 시간 범위가 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
