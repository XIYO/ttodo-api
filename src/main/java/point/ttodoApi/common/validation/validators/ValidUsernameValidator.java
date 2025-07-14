package point.ttodoApi.common.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.validation.annotations.ValidUsername;
import point.ttodoApi.common.validation.sanitizer.ValidationUtils;

@Component
@RequiredArgsConstructor
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private final ValidationUtils validationUtils;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        return validationUtils.isValidUsername(value) && 
               !validationUtils.containsSqlInjectionPattern(value);
    }
}