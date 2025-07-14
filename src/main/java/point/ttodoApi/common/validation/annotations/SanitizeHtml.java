package point.ttodoApi.common.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.common.validation.validators.SanitizeHtmlValidator.class})
@Documented
public @interface SanitizeHtml {
    String message() default "HTML content contains potentially unsafe elements";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    SanitizeMode mode() default SanitizeMode.STANDARD;
    
    enum SanitizeMode {
        STANDARD,
        STRICT,
        NONE
    }
}