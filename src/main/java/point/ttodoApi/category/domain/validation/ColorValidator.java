package point.ttodoApi.category.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static point.ttodoApi.category.domain.CategoryConstants.HEX_COLOR_PATTERN;

/**
 * 색상 유효성 검증 구현
 * #RRGGBB 형식의 16진수 색상 코드를 검증합니다.
 */
public class ColorValidator implements ConstraintValidator<ValidColor, String> {
    
    private static final Pattern HEX_COLOR_REGEX = Pattern.compile(HEX_COLOR_PATTERN);
    
    @Override
    public void initialize(ValidColor constraintAnnotation) {
        // 초기화 로직이 필요한 경우 구현
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null인 경우 유효함 (선택값)
        if (value == null) {
            return true;
        }
        
        // 빈 문자열인 경우 유효하지 않음
        if (value.isBlank()) {
            return false;
        }
        
        // #RRGGBB 형식 검증
        return HEX_COLOR_REGEX.matcher(value).matches();
    }
}
