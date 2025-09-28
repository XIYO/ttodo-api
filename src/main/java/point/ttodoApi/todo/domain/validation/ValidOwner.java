package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;

import static point.ttodoApi.todo.domain.TodoConstants.OWNER_REQUIRED_MESSAGE;

/**
 * Todo 소유자 유효성 검증 애노테이션
 * - 필수값 (NotNull)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull(message = OWNER_REQUIRED_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidOwner {
    String message() default "올바른 소유자가 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
