package point.ttodoApi.shared.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.ValidPhoneNumber;

import java.util.regex.Pattern;

@Component
public class ValidPhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

  private static final Pattern KR_PHONE_PATTERN = Pattern.compile("^(\\+82|0)(1[0-9])[0-9]{3,4}[0-9]{4}$");
  private String region;

  @Override
  public void initialize(ValidPhoneNumber constraintAnnotation) {
    this.region = constraintAnnotation.region();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }

    if (!"KR".equals(region)) {
      return true;
    }

    var normalized = value.replaceAll("[\\s-]", "");
    return KR_PHONE_PATTERN.matcher(normalized).matches();
  }
}