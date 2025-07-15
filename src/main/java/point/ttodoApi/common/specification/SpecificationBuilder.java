package point.ttodoApi.common.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import point.ttodoApi.common.error.BusinessException;

import java.util.*;

/**
 * Specification 빌더 - 체이닝 방식으로 동적 쿼리 구성
 */
@Slf4j
public class SpecificationBuilder<T> {
    
    private final List<SearchCriteria> criteriaList;
    private final BaseSpecification<T> baseSpecification;
    
    public SpecificationBuilder(BaseSpecification<T> baseSpecification) {
        this.criteriaList = new ArrayList<>();
        this.baseSpecification = baseSpecification;
    }
    
    /**
     * 검색 조건 추가
     */
    public SpecificationBuilder<T> with(SearchCriteria criteria) {
        if (criteria != null) {
            criteriaList.add(criteria);
        }
        return this;
    }
    
    /**
     * 간단한 EQUALS 조건 추가
     */
    public SpecificationBuilder<T> with(String key, Object value) {
        if (key != null && value != null) {
            criteriaList.add(SearchCriteria.of(key, value));
        }
        return this;
    }
    
    /**
     * 조건부 검색 조건 추가
     */
    public SpecificationBuilder<T> withIf(boolean condition, SearchCriteria criteria) {
        if (condition && criteria != null) {
            criteriaList.add(criteria);
        }
        return this;
    }
    
    /**
     * 조건부 EQUALS 조건 추가
     */
    public SpecificationBuilder<T> withIf(boolean condition, String key, Object value) {
        if (condition && key != null && value != null) {
            criteriaList.add(SearchCriteria.of(key, value));
        }
        return this;
    }
    
    /**
     * LIKE 조건 추가
     */
    public SpecificationBuilder<T> withLike(String key, String value) {
        if (key != null && value != null && !value.trim().isEmpty()) {
            criteriaList.add(SearchCriteria.like(key, value));
        }
        return this;
    }
    
    /**
     * IN 조건 추가
     */
    public SpecificationBuilder<T> withIn(String key, Collection<?> values) {
        if (key != null && values != null && !values.isEmpty()) {
            criteriaList.add(SearchCriteria.builder()
                    .key(key)
                    .operation(SearchOperator.IN)
                    .value(values)
                    .build());
        }
        return this;
    }
    
    /**
     * BETWEEN 조건 추가
     */
    public SpecificationBuilder<T> withBetween(String key, Object start, Object end) {
        if (key != null && start != null && end != null) {
            criteriaList.add(SearchCriteria.between(key, start, end));
        }
        return this;
    }
    
    /**
     * 날짜 범위 조건 추가 (시작일과 종료일 모두 포함)
     */
    public SpecificationBuilder<T> withDateRange(String key, Object startDate, Object endDate) {
        if (key != null) {
            if (startDate != null) {
                criteriaList.add(SearchCriteria.builder()
                        .key(key)
                        .operation(SearchOperator.GREATER_THAN_OR_EQUALS)
                        .value(startDate)
                        .build());
            }
            if (endDate != null) {
                criteriaList.add(SearchCriteria.builder()
                        .key(key)
                        .operation(SearchOperator.LESS_THAN_OR_EQUALS)
                        .value(endDate)
                        .build());
            }
        }
        return this;
    }
    
    /**
     * NULL 체크 조건 추가
     */
    public SpecificationBuilder<T> withNull(String key) {
        if (key != null) {
            criteriaList.add(SearchCriteria.isNull(key));
        }
        return this;
    }
    
    /**
     * NOT NULL 체크 조건 추가
     */
    public SpecificationBuilder<T> withNotNull(String key) {
        if (key != null) {
            criteriaList.add(SearchCriteria.isNotNull(key));
        }
        return this;
    }
    
    /**
     * 최종 Specification 생성
     */
    public Specification<T> build() {
        if (criteriaList.isEmpty()) {
            return Specification.where(null);
        }
        
        try {
            return baseSpecification.buildSpecification(criteriaList);
        } catch (Exception e) {
            log.error("Failed to build specification", e);
            throw new BusinessException("Invalid search criteria");
        }
    }
    
    /**
     * 검색 조건 개수 반환
     */
    public int getCriteriaCount() {
        return criteriaList.size();
    }
    
    /**
     * 검색 조건이 있는지 확인
     */
    public boolean hasCriteria() {
        return !criteriaList.isEmpty();
    }
    
    /**
     * 검색 조건 초기화
     */
    public SpecificationBuilder<T> clear() {
        criteriaList.clear();
        return this;
    }
}