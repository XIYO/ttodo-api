package point.ttodoApi.shared.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.validation.annotations.SafeUrl;
import point.ttodoApi.shared.validation.threat.InputThreatPattern;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SafeUrlValidator implements ConstraintValidator<SafeUrl, String> {

  private static final Pattern URL_PATTERN = Pattern.compile(
      "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$"
  );
  private boolean allowHttp;

  @Override
  public void initialize(SafeUrl constraintAnnotation) {
    this.allowHttp = constraintAnnotation.allowHttp();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }

    if (!URL_PATTERN.matcher(value).matches()) {
      return false;
    }

    if (!allowHttp && value.toLowerCase(Locale.ROOT).startsWith("http://")) {
      return false;
    }

    return !InputThreatPattern.SQL_INJECTION.matcher(value).matches();
  }
}