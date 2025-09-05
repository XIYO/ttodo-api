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

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^.{4,}$"  // 4자 이상만 확인
    );
    
    // Common weak passwords to block
    private static final String[] COMMON_WEAK_PASSWORDS = {
            "password", "12345678", "123456789", "1234567890", "qwerty", "qwertyuiop",
            "admin123", "password123", "admin@123", "test123", "demo123", "welcome123",
            "password1", "p@ssw0rd", "p@ssword", "passw0rd", "qwerty123"
    };

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
    
    private boolean hasConsecutiveCharacters(String password, int maxConsecutive) {
        if (password.length() < maxConsecutive) {
            return false;
        }
        
        for (int i = 0; i <= password.length() - maxConsecutive; i++) {
            boolean ascending = true;
            boolean descending = true;
            
            for (int j = 0; j < maxConsecutive - 1; j++) {
                char current = password.charAt(i + j);
                char next = password.charAt(i + j + 1);
                
                // Check for ascending sequence
                if (next != current + 1) {
                    ascending = false;
                }
                
                // Check for descending sequence
                if (next != current - 1) {
                    descending = false;
                }
                
                // If neither ascending nor descending, break early
                if (!ascending && !descending) {
                    break;
                }
            }
            
            if (ascending || descending) {
                return true;
            }
        }
        
        return false;
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