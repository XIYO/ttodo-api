package point.ttodoApi.challenge.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.*;
import java.util.regex.Pattern;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

/**
 * 초대 코드 유효성 검증 애노테이션
 * - 8자 대문자 또는 숫자
 * - null 허용 (선택사항)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidInviteCode.InviteCodeValidator.class)
public @interface ValidInviteCode {
    String message() default "초대 코드는 " + INVITE_CODE_LENGTH + "자의 대문자 또는 숫자여야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class InviteCodeValidator implements ConstraintValidator<ValidInviteCode, String> {
        private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{" + INVITE_CODE_LENGTH + "}$");
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // null은 허용 (선택사항)
            if (value == null) {
                return true;
            }
            
            return INVITE_CODE_PATTERN.matcher(value).matches();
        }
    }
}