package point.ttodoApi.shared.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.SecurePassword;

import java.util.regex.Pattern;

@Component
public class SecurePasswordValidator implements ConstraintValidator<SecurePassword, String> {
  private static final int MIN_LENGTH = 8;
  private static final Pattern LETTER_PATTERN = Pattern.compile("[A-Za-z]");
  private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }

    if (value.length() < MIN_LENGTH) {
      return false;
    }

    return LETTER_PATTERN.matcher(value).find()
        && DIGIT_PATTERN.matcher(value).find();
  }
}