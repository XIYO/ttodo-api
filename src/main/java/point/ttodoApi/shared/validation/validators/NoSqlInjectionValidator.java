package point.ttodoApi.shared.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.NoSqlInjection;
import point.ttodoApi.shared.validation.threat.InputThreatPattern;

@Component
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }

    return !InputThreatPattern.SQL_INJECTION.matcher(value).matches();
  }
}