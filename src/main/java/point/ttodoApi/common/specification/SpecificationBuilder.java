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
     * 조건부 복잡한 조건 추가 (Function을 사용하여 여러 조건을 체이닝)
     */
    public SpecificationBuilder<T> withIf(boolean condition, 
                                         java.util.function.Function<SpecificationBuilder<T>, SpecificationBuilder<T>> builderFunction) {
        if (condition) {
            return builderFunction.apply(this);
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
     * IS NOT NULL 조건 추가
     */
    public SpecificationBuilder<T> isNotNull(String key) {
        if (key != null) {
            criteriaList.add(SearchCriteria.builder()
                    .key(key)
                    .operation(SearchOperator.IS_NOT_NULL)
                    .value(null)
                    .build());
        }
        return this;
    }
    
    /**
     * LESS THAN OR EQUAL 조건 추가
     */
    public SpecificationBuilder<T> lessThanOrEqual(String key, Object value) {
        if (key != null && value != null) {
            criteriaList.add(SearchCriteria.builder()
                    .key(key)
                    .operation(SearchOperator.LESS_THAN_OR_EQUAL)
                    .value(value)
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
     * GREATER_THAN_OR_EQUALS 조건 추가
     */
    public SpecificationBuilder<T> greaterThanOrEqual(String key, Object value) {
        if (key != null && value != null) {
            criteriaList.add(SearchCriteria.builder()
                    .key(key)
                    .operation(SearchOperator.GREATER_THAN_OR_EQUALS)
                    .value(value)
                    .build());
        }
        return this;
    }
    
    /**
     * LESS_THAN 조건 추가
     */
    public SpecificationBuilder<T> lessThan(String key, Object value) {
        if (key != null && value != null) {
            criteriaList.add(SearchCriteria.builder()
                    .key(key)
                    .operation(SearchOperator.LESS_THAN)
                    .value(value)
                    .build());
        }
        return this;
    }
    
    /**
     * isNull 메서드 - withNull의 별칭
     */
    public SpecificationBuilder<T> isNull(String key) {
        return withNull(key);
    }
    
    /**
     * 최종 Specification 생성
     */
    public Specification<T> build() {
        if (criteriaList.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
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
    
    /**
     * OR 조건 그룹 추가
     * 람다 표현식을 통해 OR로 연결될 조건들을 정의
     * 
     * 사용 예:
     * builder.or(orBuilder -> orBuilder
     *     .with("status", "ACTIVE")
     *     .with("status", "PENDING")
     * )
     */
    public SpecificationBuilder<T> or(java.util.function.Function<SpecificationBuilder<T>, SpecificationBuilder<T>> orFunction) {
        // 새로운 빌더를 만들어 OR 조건들을 수집
        SpecificationBuilder<T> orBuilder = new SpecificationBuilder<>(baseSpecification);
        orFunction.apply(orBuilder);
        
        // OR 조건들이 있을 경우에만 처리
        if (orBuilder.hasCriteria()) {
            // OR 그룹을 표현하기 위한 특별한 SearchCriteria 생성
            SearchCriteria orCriteria = SearchCriteria.builder()
                    .key("OR_GROUP")
                    .operation(SearchOperator.OR_GROUP)
                    .value(orBuilder.criteriaList)
                    .build();
            criteriaList.add(orCriteria);
        }
        
        return this;
    }
}