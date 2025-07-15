package point.ttodoApi.common.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.common.validation.validators.ValidEmailValidator.class})
@Documented
public @interface ValidEmail {
    String message() default "Invalid email address or email domain is not allowed";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    boolean allowDisposable() default false;
}