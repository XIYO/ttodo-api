package point.ttodoApi.todo.domain.validation.optional;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * Todo 제목 검증 어노테이션 (수정시 선택적)
 * TTODO 아키텍처 패턴 적용
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 255, message = "Todo 제목은 최대 255자까지 입력 가능합니다")
@Documented
public @interface OptionalTodoTitle {
    String message() default "유효하지 않은 Todo 제목입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}