package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static point.ttodoApi.todo.domain.TodoConstants.*;

/**
 * Todo 제목 유효성 검증 애노테이션 (필수)
 * - 필수값 (NotBlank)
 * - 1자 이상 255자 이하
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = TITLE_REQUIRED_MESSAGE)
@Size(min = 1, max = TITLE_MAX_LENGTH, message = TITLE_SIZE_MESSAGE)
@Constraint(validatedBy = {})
public @interface ValidTitle {
    String message() default "올바른 제목 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
