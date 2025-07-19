package point.ttodoApi.common.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pageable 파라미터 자동 검증 Aspect
 * @ValidPageable 어노테이션이 붙은 메서드의 Pageable 파라미터를 자동으로 검증
 */
@Slf4j
@Aspect
@Component
@Order(1) // 다른 Aspect보다 먼저 실행
@RequiredArgsConstructor
public class PageableValidationAspect {
    
    private final PageableValidator pageableValidator;
    
    /**
     * @ValidPageable 어노테이션이 붙은 메서드 실행 전 Pageable 검증
     */
    @Before("@annotation(validPageable)")
    public void validatePageable(JoinPoint joinPoint, ValidPageable validPageable) {
        // 메서드 파라미터에서 Pageable 찾기
        Object[] args = joinPoint.getArgs();
        Class<?>[] paramTypes = Arrays.stream(joinPoint.getSignature().getDeclaringType().getMethods())
                .filter(m -> m.getName().equals(joinPoint.getSignature().getName()))
                .findFirst()
                .map(m -> m.getParameterTypes())
                .orElse(new Class<?>[0]);
        
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Pageable) {
                Pageable pageable = (Pageable) args[i];
                
                // 허용된 정렬 필드 가져오기
                Set<String> allowedSortFields = getAllowedSortFields(validPageable.sortFields());
                
                // 검증 수행
                pageableValidator.validate(pageable, allowedSortFields);
                
                if (log.isDebugEnabled()) {
                    log.debug("Validated pageable: page={}, size={}, sort={}", 
                            pageable.getPageNumber(), 
                            pageable.getPageSize(), 
                            pageable.getSort());
                }
                
                break; // 첫 번째 Pageable만 검증
            }
        }
    }
    
    /**
     * SortFieldsProvider enum에서 허용된 정렬 필드 가져오기
     */
    private Set<String> getAllowedSortFields(SortFieldsProvider provider) {
        return switch (provider) {
            case TODO -> AllowedSortFields.TODO_FIELDS;
            case CATEGORY -> AllowedSortFields.CATEGORY_FIELDS;
            case MEMBER -> AllowedSortFields.MEMBER_FIELDS;
            case CHALLENGE -> AllowedSortFields.CHALLENGE_FIELDS;
            case CHALLENGE_TODO -> AllowedSortFields.CHALLENGE_TODO_FIELDS;
            case COMMON -> AllowedSortFields.COMMON_FIELDS;
            case NONE -> Set.of(); // 정렬 허용 안 함
        };
    }
}