package point.ttodoApi.user.domain.validation;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
  private final UserRepository UserRepository;

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (email == null || email.isEmpty()) {
      return true;
    }
    return !UserRepository.existsByEmail(email);
  }
}
