package point.ttodoApi.user.domain.validation.required;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;

/**
 * ValidUserId 어노테이션의 실제 검증 로직 구현
 */
public class ValidUserIdValidator implements ConstraintValidator<ValidUserId, UUID> {
    
    @Override
    public void initialize(ValidUserId constraintAnnotation) {
        // 초기화 로직이 필요한 경우 여기에 작성
    }
    
    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        // null 값은 @NotNull로 별도 처리하므로 여기서는 true 반환
        if (value == null) {
            return true;
        }
        
        // UUID 형식 검증 (실제로는 UUID 타입이므로 항상 유효함)
        return true;
    }
}