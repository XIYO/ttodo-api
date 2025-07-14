package point.ttodoApi.common.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.common.validation.validators.ValidUsernameValidator.class})
@Documented
public @interface ValidUsername {
    String message() default "Username must be 2-20 characters long and can only contain letters, numbers, Korean characters, dots, underscores, and hyphens";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}