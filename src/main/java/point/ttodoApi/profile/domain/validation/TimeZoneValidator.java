package point.ttodoApi.profile.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;
import java.util.Set;

/**
 * 타임존 유효성 검증 구현
 */
public class TimeZoneValidator implements ConstraintValidator<ValidTimeZone, String> {
    
    private static final Set<String> AVAILABLE_ZONE_IDS = ZoneId.getAvailableZoneIds();
    
    @Override
    public void initialize(ValidTimeZone constraintAnnotation) {
        // 초기화 로직이 필요한 경우 구현
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 빈 문자열인 경우 @NotBlank에서 처리
        if (value == null || value.isBlank()) {
            return true;
        }
        
        // 유효한 타임존인지 확인
        return AVAILABLE_ZONE_IDS.contains(value);
    }
}