package point.ttodoApi.shared.validation.validators;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.NoSqlInjection;
import point.ttodoApi.shared.validation.sanitizer.ValidationUtils;

@Component
@RequiredArgsConstructor
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

  private final ValidationUtils validationUtils;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }

    return !validationUtils.containsSqlInjectionPattern(value);
  }
}