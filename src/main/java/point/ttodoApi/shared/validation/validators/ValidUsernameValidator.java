package point.ttodoApi.shared.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.ValidUsername;
import point.ttodoApi.shared.validation.service.ForbiddenWordService;
import point.ttodoApi.shared.validation.threat.InputThreatPattern;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

  private final ForbiddenWordService forbiddenWordService;
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣._-]{2,20}$");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return false;
    }

    // Check basic username pattern
    if (!USERNAME_PATTERN.matcher(value).matches()) {
      return false;
    }

    // Check for SQL injection patterns
    if (InputThreatPattern.SQL_INJECTION.matcher(value).matches()) {
      return false;
    }

    // Check for forbidden words
    if (forbiddenWordService.containsForbiddenWord(value)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Username contains forbidden words")
              .addConstraintViolation();
      return false;
    }

    return true;
  }
}