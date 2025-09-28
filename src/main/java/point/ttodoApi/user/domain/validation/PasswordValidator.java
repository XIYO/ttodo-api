package point.ttodoApi.user.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 비밀번호 유효성 검증 구현
 * 영문, 숫자, 특수문자를 모두 포함해야 함
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    // 영문, 숫자, 특수문자를 모두 포함하는 패턴
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&]).*$"
    );
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // 초기화 로직이 필요한 경우 구현
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 빈 문자열인 경우 @NotBlank에서 처리
        if (value == null || value.isBlank()) {
            return true;
        }
        
        // 비밀번호 패턴 검증
        return PASSWORD_PATTERN.matcher(value).matches();
    }
}
