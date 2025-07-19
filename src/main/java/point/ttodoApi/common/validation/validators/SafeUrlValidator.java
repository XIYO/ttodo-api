package point.ttodoApi.common.validation.validators;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.validation.annotations.SafeUrl;
import point.ttodoApi.common.validation.sanitizer.ValidationUtils;

@Component
@RequiredArgsConstructor
public class SafeUrlValidator implements ConstraintValidator<SafeUrl, String> {

    private final ValidationUtils validationUtils;
    private boolean allowHttp;

    @Override
    public void initialize(SafeUrl constraintAnnotation) {
        this.allowHttp = constraintAnnotation.allowHttp();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        if (!validationUtils.isValidUrl(value)) {
            return false;
        }

        if (!allowHttp && value.toLowerCase().startsWith("http://")) {
            return false;
        }

        return !validationUtils.containsSqlInjectionPattern(value);
    }
}