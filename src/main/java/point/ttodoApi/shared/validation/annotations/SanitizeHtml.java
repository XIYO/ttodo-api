package point.ttodoApi.shared.validation.annotations;

import jakarta.validation.*;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.shared.validation.validators.SanitizeHtmlValidator.class})
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