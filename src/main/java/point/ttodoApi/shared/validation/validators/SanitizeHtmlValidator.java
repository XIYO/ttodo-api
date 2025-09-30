package point.ttodoApi.shared.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.SanitizeHtml;

@Component
public class SanitizeHtmlValidator implements ConstraintValidator<SanitizeHtml, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return true;
  }
}