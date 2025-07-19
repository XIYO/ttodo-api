package point.ttodoApi.common.validation.annotations;

import jakarta.validation.*;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.common.validation.validators.NoSqlInjectionValidator.class})
@Documented
public @interface NoSqlInjection {
    String message() default "Input contains potentially unsafe SQL patterns";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}