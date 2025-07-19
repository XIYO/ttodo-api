package point.ttodoApi.common.validation.validators;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.validation.annotations.SanitizeHtml;
import point.ttodoApi.common.validation.sanitizer.ValidationUtils;

@Component
@RequiredArgsConstructor
public class SanitizeHtmlValidator implements ConstraintValidator<SanitizeHtml, String> {

    private final ValidationUtils validationUtils;
    private SanitizeHtml.SanitizeMode mode;

    @Override
    public void initialize(SanitizeHtml constraintAnnotation) {
        this.mode = constraintAnnotation.mode();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        String sanitized = switch (mode) {
            case STANDARD -> validationUtils.sanitizeHtml(value);
            case STRICT -> validationUtils.sanitizeHtmlStrict(value);
            case NONE -> validationUtils.stripHtml(value);
        };

        return value.equals(sanitized);
    }
}