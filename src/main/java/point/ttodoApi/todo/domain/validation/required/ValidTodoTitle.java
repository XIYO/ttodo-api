package point.ttodoApi.todo.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * Todo 제목 검증 어노테이션 (생성시 필수)
 * TTODO 아키텍처 패턴 적용
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "Todo 제목은 필수 입력값입니다")
@Size(min = 1, max = 255, message = "Todo 제목은 1-255자 사이여야 합니다")
@Documented
public @interface ValidTodoTitle {
    String message() default "유효하지 않은 Todo 제목입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}