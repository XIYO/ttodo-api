package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static point.ttodoApi.todo.domain.TodoConstants.*;

/**
 * Todo 설명 유효성 검증 애노테이션 (선택)
 * - 선택값 (null 허용)
 * - 최대 1000자
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = DESCRIPTION_MAX_LENGTH, message = DESCRIPTION_SIZE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidDescription {
    String message() default "올바른 설명 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
