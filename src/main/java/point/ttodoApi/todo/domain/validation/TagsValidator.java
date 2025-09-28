package point.ttodoApi.todo.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

import static point.ttodoApi.todo.domain.TodoConstants.*;

/**
 * 태그 유효성 검증 구현
 */
public class TagsValidator implements ConstraintValidator<ValidTags, Set<String>> {
    
    @Override
    public void initialize(ValidTags constraintAnnotation) {
        // 초기화 로직이 필요한 경우 구현
    }
    
    @Override
    public boolean isValid(Set<String> tags, ConstraintValidatorContext context) {
        // null이거나 빈 세트인 경우 유효
        if (tags == null || tags.isEmpty()) {
            return true;
        }
        
        // 태그 개수 확인
        if (tags.size() > MAX_TAGS_COUNT) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(TAG_COUNT_MESSAGE)
                    .addConstraintViolation();
            return false;
        }
        
        // 각 태그 검증
        for (String tag : tags) {
            // null이거나 빈 문자열 확인
            if (tag == null || tag.trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("빈 태그는 허용되지 않습니다")
                        .addConstraintViolation();
                return false;
            }
            
            // 태그 길이 확인
            if (tag.length() > TAG_MAX_LENGTH) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(TAG_SIZE_MESSAGE)
                        .addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}
