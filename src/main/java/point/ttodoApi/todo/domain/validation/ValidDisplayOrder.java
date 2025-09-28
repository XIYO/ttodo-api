package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

import static point.ttodoApi.todo.domain.TodoConstants.*;

/**
 * 순서 유효성 검증 애노테이션
 * - 0 이상 999999 이하
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Min(value = DISPLAY_ORDER_MIN_VALUE, message = DISPLAY_ORDER_RANGE_MESSAGE)
@Max(value = DISPLAY_ORDER_MAX_VALUE, message = DISPLAY_ORDER_RANGE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidDisplayOrder {
    String message() default "올바른 순서가 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
