package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

import static point.ttodoApi.todo.domain.TodoConstants.*;

/**
 * 우선순위 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - 0(Low), 1(Normal), 2(High)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Min(value = PRIORITY_MIN_VALUE, message = PRIORITY_RANGE_MESSAGE)
@Max(value = PRIORITY_MAX_VALUE, message = PRIORITY_RANGE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidTodoPriority {
    String message() default "올바른 우선순위가 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
