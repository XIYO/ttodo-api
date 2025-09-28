package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

import static point.ttodoApi.profile.domain.StatisticsConstants.*;

/**
 * 성공한 TODO 수 유효성 검증 애노테이션
 * - 0 이상 20억 이하
 * - null 허용 (기본값 0으로 처리)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Min(value = MIN_COUNT, message = SUCCEEDED_TODOS_COUNT_MESSAGE)
@Max(value = MAX_COUNT, message = SUCCEEDED_TODOS_COUNT_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidSucceededTodosCount {
    String message() default "올바른 성공 TODO 수 범위가 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
