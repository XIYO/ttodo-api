package point.ttodoApi.shared.validation.validators;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.ValidEmail;
import point.ttodoApi.shared.validation.service.DisposableEmailService;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

  // RFC 5322 compliant email regex
  private static final Pattern RFC5322_PATTERN = Pattern.compile(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                  "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
  );

  private final DisposableEmailService disposableEmailService;

  private boolean allowDisposable;

  @Override
  public void initialize(ValidEmail constraintAnnotation) {
    this.allowDisposable = constraintAnnotation.allowDisposable();
  }

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (email == null || email.isEmpty()) {
      return false;
    }

    // Normalize email (lowercase and trim)
    email = email.toLowerCase().trim();

    // Check RFC 5322 compliance
    if (!RFC5322_PATTERN.matcher(email).matches()) {
      return false;
    }

    // (중복 제거) ValidationUtils.isValidEmail 호출 제거
    // SQL Injection 패턴 검사는 별도 유틸 이관 예정 - 1단계에서는 생략

    // Check for disposable email domains if not allowed
    if (!allowDisposable && disposableEmailService.isDisposableEmail(email)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Disposable email addresses are not allowed")
              .addConstraintViolation();
      return false;
    }

    // Check local part length (before @)
    String localPart = email.substring(0, email.indexOf('@'));
    if (localPart.length() > 64) {
      return false;
    }

    // Check domain part length (after @)
    String domainPart = email.substring(email.indexOf('@') + 1);
    if (domainPart.length() > 255) {
      return false;
    }

    return true;
  }
}