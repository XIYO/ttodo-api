package point.zzicback.common.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import point.zzicback.auth.application.AuthService;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
private final AuthService authService;

@Autowired
public UniqueEmailValidator(AuthService authService) {
  this.authService = authService;
}

@Override
public boolean isValid(String email, ConstraintValidatorContext context) {
  if (email == null || email.isEmpty()) {
    return true;
  }
  return ! authService.isEmailTaken(email);
}
}
