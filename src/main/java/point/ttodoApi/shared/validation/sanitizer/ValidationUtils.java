package point.ttodoApi.shared.validation.sanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationUtils {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                  "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
  );

  private static final Pattern PHONE_PATTERN = Pattern.compile(
          "^(\\+82|0)(1[0-9])[0-9]{3,4}[0-9]{4}$"
  );

  private static final Pattern USERNAME_PATTERN = Pattern.compile(
          "^[a-zA-Z0-9가-힣._-]{2,20}$"
  );


  private static final Pattern URL_PATTERN = Pattern.compile(
          "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$"
  );

  private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
          ".*(;|--|'|\"|\\*|xp_|sp_|<script|</script|<iframe|</iframe|javascript:|onclick=|onerror=|onload=).*",
          Pattern.CASE_INSENSITIVE
  );

  @Qualifier("htmlSanitizerPolicy")
  private final PolicyFactory htmlSanitizerPolicy;

  @Qualifier("strictHtmlSanitizerPolicy")
  private final PolicyFactory strictHtmlSanitizerPolicy;

  @Qualifier("noHtmlPolicy")
  private final PolicyFactory noHtmlPolicy;

  public String sanitizeHtml(String input) {
    if (input == null) {
      return null;
    }
    return htmlSanitizerPolicy.sanitize(input);
  }

  public String sanitizeHtmlStrict(String input) {
    if (input == null) {
      return null;
    }
    return strictHtmlSanitizerPolicy.sanitize(input);
  }

  public String stripHtml(String input) {
    if (input == null) {
      return null;
    }
    return noHtmlPolicy.sanitize(input);
  }

  public boolean isValidEmail(String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  public boolean isValidPhoneNumber(String phone) {
    if (phone == null) {
      return false;
    }
    String cleaned = phone.replaceAll("[\\s-]", "");
    return PHONE_PATTERN.matcher(cleaned).matches();
  }

  public boolean isValidUsername(String username) {
    return username != null && USERNAME_PATTERN.matcher(username).matches();
  }

  public boolean isValidPassword(String password) {
    if (password == null || password.length() < 4) {
      return false;
    }

    return true;
  }

  public boolean isValidUrl(String url) {
    return url != null && URL_PATTERN.matcher(url).matches();
  }

  public boolean containsSqlInjectionPattern(String input) {
    if (input == null) {
      return false;
    }
    return SQL_INJECTION_PATTERN.matcher(input).matches();
  }

  public String sanitizeForLog(String input) {
    if (input == null) {
      return null;
    }
    return input.replaceAll("[\r\n]", "_");
  }

  public String truncate(String input, int maxLength) {
    if (input == null || input.length() <= maxLength) {
      return input;
    }
    return input.substring(0, maxLength);
  }

  public boolean isWithinLength(String input, int minLength, int maxLength) {
    if (input == null) {
      return minLength == 0;
    }
    int length = input.length();
    return length >= minLength && length <= maxLength;
  }

  public String normalizeWhitespace(String input) {
    if (input == null) {
      return null;
    }
    return input.trim().replaceAll("\\s+", " ");
  }
}