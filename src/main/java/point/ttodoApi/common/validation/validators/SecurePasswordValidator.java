package point.ttodoApi.common.validation.validators;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.validation.annotations.SecurePassword;
import point.ttodoApi.common.validation.sanitizer.ValidationUtils;

@Component
@RequiredArgsConstructor
public class SecurePasswordValidator implements ConstraintValidator<SecurePassword, String> {

    private final ValidationUtils validationUtils;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return validationUtils.isValidPassword(value);
    }
}