package point.ttodoApi.shared.validation.annotations;

import jakarta.validation.*;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.shared.validation.validators.SecurePasswordValidator.class})
@Documented
public @interface SecurePassword {
  String message() default "Password must be at least 4 characters long";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}