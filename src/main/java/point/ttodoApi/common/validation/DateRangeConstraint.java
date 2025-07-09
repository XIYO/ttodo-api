package point.ttodoApi.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface DateRangeConstraint {
    String message() default "startDate와 endDate는 함께 제공되어야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}