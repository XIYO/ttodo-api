package point.ttodoApi.shared.validation.annotations;

import jakarta.validation.*;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {point.ttodoApi.shared.validation.validators.ValidPhoneNumberValidator.class})
@Documented
public @interface ValidPhoneNumber {
  String message() default "Invalid phone number format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String region() default "KR";
}