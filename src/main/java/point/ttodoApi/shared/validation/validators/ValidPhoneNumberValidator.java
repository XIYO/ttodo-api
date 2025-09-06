package point.ttodoApi.shared.validation.validators;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.ValidPhoneNumber;
import point.ttodoApi.shared.validation.sanitizer.ValidationUtils;

@Component
@RequiredArgsConstructor
public class ValidPhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

  private final ValidationUtils validationUtils;
  private String region;

  @Override
  public void initialize(ValidPhoneNumber constraintAnnotation) {
    this.region = constraintAnnotation.region();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }

    if (!"KR".equals(region)) {
      return true;
    }

    return validationUtils.isValidPhoneNumber(value);
  }
}