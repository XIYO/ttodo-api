package point.ttodoApi.common.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.error.BusinessException;

import java.util.regex.Pattern;

/**
 * 정렬 필드 검증을 위한 유틸리티
 */
@Slf4j
@Component
public class SortValidator {
    
    // 안전한 필드명 패턴 (알파벳, 숫자, 언더스코어, 점만 허용)
    private static final Pattern SAFE_FIELD_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$");
    
    /**
     * Sort 객체의 모든 필드 검증
     */
    public void validateSort(Sort sort, BaseSpecification<?> specification) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }
        
        sort.forEach(order -> {
            String property = order.getProperty();
            validateSortField(property, specification);
        });
    }
    
    /**
     * 단일 정렬 필드 검증
     */
    public void validateSortField(String field, BaseSpecification<?> specification) {
        // null 또는 빈 문자열 체크
        if (field == null || field.trim().isEmpty()) {
            throw new BusinessException("Sort field cannot be empty");
        }
        
        // SQL Injection 방지를 위한 패턴 검증
        if (!SAFE_FIELD_PATTERN.matcher(field).matches()) {
            log.warn("Invalid sort field pattern detected: {}", field);
            throw new BusinessException("Invalid sort field: " + field);
        }
        
        // 화이트리스트 검증
        specification.validateSortField(field);
    }
    
    /**
     * 정렬 필드 sanitization
     */
    public String sanitizeSortField(String field) {
        if (field == null) {
            return null;
        }
        
        // 위험한 문자 제거
        String sanitized = field.replaceAll("[^a-zA-Z0-9._]", "");
        
        // 빈 문자열이 되었다면 에러
        if (sanitized.isEmpty()) {
            throw new BusinessException("Invalid sort field after sanitization");
        }
        
        return sanitized;
    }
    
    /**
     * Sort.Direction 검증
     */
    public void validateSortDirection(String direction) {
        if (direction == null || direction.isEmpty()) {
            return;
        }
        
        String upperDirection = direction.toUpperCase();
        if (!upperDirection.equals("ASC") && !upperDirection.equals("DESC")) {
            throw new BusinessException("Invalid sort direction: " + direction);
        }
    }
}