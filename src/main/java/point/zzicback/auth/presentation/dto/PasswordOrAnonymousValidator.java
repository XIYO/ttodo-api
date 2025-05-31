package point.zzicback.auth.presentation.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordOrAnonymousValidator implements ConstraintValidator<PasswordOrAnonymousValid, SignInRequest> {
private static final String ANONYMOUS_EMAIL = "anonymous@shared.com";

@Override
public boolean isValid(SignInRequest value, ConstraintValidatorContext context) {
  if (value == null)
    return true;
  if (ANONYMOUS_EMAIL.equals(value.email())) {
    return true;
  }
  return value.password() != null && ! value.password().isBlank();
}
}
