package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ByRulesValidator.class)
public @interface ValidByRules {
    String message() default "유효하지 않은 BY 규칙입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}