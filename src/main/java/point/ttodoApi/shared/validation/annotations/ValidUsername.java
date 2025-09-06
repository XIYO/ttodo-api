package point.ttodoApi.shared.validation.annotations;

import jakarta.validation.*;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.shared.validation.validators.ValidUsernameValidator.class})
@Documented
public @interface ValidUsername {
  String message() default "Username must be 2-20 characters long, can only contain letters, numbers, Korean characters, dots, underscores, and hyphens, and must not contain forbidden words";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}