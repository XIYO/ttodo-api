package point.zzicback.member.presentation.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordOrAnonymousValidator implements ConstraintValidator<PasswordOrAnonymousValid, SignInRequest> {
    private static final String ANONYMOUS_EMAIL = "anonymous@shared.com";

    @Override
    public boolean isValid(SignInRequest value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (ANONYMOUS_EMAIL.equals(value.email())) {
            // anonymous 사용자는 password 없어도 통과
            return true;
        }
        // 일반 사용자는 password가 null/blank 아니어야 함
        return value.password() != null && !value.password().isBlank();
    }
}
