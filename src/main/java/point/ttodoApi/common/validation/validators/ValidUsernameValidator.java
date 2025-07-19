package point.ttodoApi.common.validation.validators;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.validation.annotations.ValidUsername;
import point.ttodoApi.common.validation.sanitizer.ValidationUtils;
import point.ttodoApi.common.validation.service.ForbiddenWordService;

@Component
@RequiredArgsConstructor
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private final ValidationUtils validationUtils;
    private final ForbiddenWordService forbiddenWordService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        // Check basic username pattern
        if (!validationUtils.isValidUsername(value)) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (validationUtils.containsSqlInjectionPattern(value)) {
            return false;
        }
        
        // Check for forbidden words
        if (forbiddenWordService.containsForbiddenWord(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Username contains forbidden words")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}