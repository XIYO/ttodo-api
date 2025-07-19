package point.ttodoApi.common.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.error.*;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pageable 파라미터 검증을 위한 유틸리티
 * 페이지 크기, 페이지 번호, 정렬 필드 등의 유효성을 검증
 */
@Slf4j
@Component
public class PageableValidator {
    
    // 최대 페이지 크기
    private static final int MAX_PAGE_SIZE = 100;
    
    // 최소 페이지 크기
    private static final int MIN_PAGE_SIZE = 1;
    
    // 최대 페이지 번호 (메모리 보호를 위해 제한)
    private static final int MAX_PAGE_NUMBER = 10000;
    
    // 안전한 정렬 필드 패턴
    private static final Pattern SAFE_SORT_FIELD_PATTERN = 
        Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$");
    
    /**
     * Pageable 객체의 전체 검증
     * 
     * @param pageable 검증할 Pageable 객체
     * @param allowedSortFields 허용된 정렬 필드 목록
     */
    public void validate(Pageable pageable, Set<String> allowedSortFields) {
        if (pageable == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Pageable cannot be null");
        }
        
        validatePageSize(pageable.getPageSize());
        validatePageNumber(pageable.getPageNumber());
        validateSort(pageable.getSort(), allowedSortFields);
    }
    
    /**
     * 페이지 크기 검증
     * 
     * @param pageSize 페이지 크기
     */
    public void validatePageSize(int pageSize) {
        if (pageSize < MIN_PAGE_SIZE) {
            throw new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE, 
                String.format("Page size must be at least %d", MIN_PAGE_SIZE)
            );
        }
        
        if (pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE, 
                String.format("Page size cannot exceed %d", MAX_PAGE_SIZE)
            );
        }
    }
    
    /**
     * 페이지 번호 검증
     * 
     * @param pageNumber 페이지 번호
     */
    public void validatePageNumber(int pageNumber) {
        if (pageNumber < 0) {
            throw new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE, 
                "Page number cannot be negative"
            );
        }
        
        if (pageNumber > MAX_PAGE_NUMBER) {
            throw new BusinessException(
                ErrorCode.INVALID_INPUT_VALUE, 
                String.format("Page number cannot exceed %d", MAX_PAGE_NUMBER)
            );
        }
    }
    
    /**
     * 정렬 파라미터 검증
     * 
     * @param sort 정렬 정보
     * @param allowedSortFields 허용된 정렬 필드 목록
     */
    public void validateSort(Sort sort, Set<String> allowedSortFields) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }
        
        sort.forEach(order -> {
            String property = order.getProperty();
            
            // SQL Injection 방지를 위한 패턴 검증
            if (!SAFE_SORT_FIELD_PATTERN.matcher(property).matches()) {
                log.warn("Invalid sort field pattern detected: {}", property);
                throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, 
                    "Invalid sort field: " + property
                );
            }
            
            // 화이트리스트 검증
            if (allowedSortFields != null && !allowedSortFields.isEmpty() 
                && !allowedSortFields.contains(property)) {
                log.warn("Unauthorized sort field requested: {}", property);
                throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE, 
                    "Sort field not allowed: " + property
                );
            }
        });
    }
    
    /**
     * 기본 Pageable 설정 적용
     * 클라이언트가 제공하지 않은 경우 안전한 기본값 설정
     * 
     * @param page 페이지 번호 (null 가능)
     * @param size 페이지 크기 (null 가능)
     * @param sort 정렬 조건 (null 가능)
     * @return 검증된 파라미터 값들
     */
    public PageableParams applyDefaults(Integer page, Integer size, String sort) {
        int validPage = page != null ? page : 0;
        int validSize = size != null ? size : 20;
        
        // 기본값 적용 후에도 검증
        validatePageNumber(validPage);
        validatePageSize(validSize);
        
        return new PageableParams(validPage, validSize, sort);
    }
    
    /**
     * 검증된 Pageable 파라미터를 담는 레코드
     */
    public record PageableParams(int page, int size, String sort) {}
    
    /**
     * 오프셋 계산 (0부터 시작)
     * 
     * @param pageable Pageable 객체
     * @return 시작 오프셋
     */
    public long calculateOffset(Pageable pageable) {
        return (long) pageable.getPageNumber() * pageable.getPageSize();
    }
    
    /**
     * 총 페이지 수 계산
     * 
     * @param totalElements 전체 요소 수
     * @param pageSize 페이지 크기
     * @return 총 페이지 수
     */
    public int calculateTotalPages(long totalElements, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }
}