package point.ttodoApi.todo.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CompleteUpdateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCompleteUpdate {
    String message() default "완료 상태 변경 시 다른 필드와 함께 수정할 수 없습니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
