package point.ttodoApi.shared.specification;

import lombok.*;

/**
 * 동적 쿼리를 위한 검색 조건
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteria {
    
    /**
     * 검색할 필드명 (예: "title", "owner.email")
     */
    private String key;
    
    /**
     * 검색 연산자
     */
    private SearchOperator operation;
    
    /**
     * 검색 값
     */
    private Object value;
    
    /**
     * 간단한 생성자 - EQUALS 연산자 사용
     */
    public static SearchCriteria of(String key, Object value) {
        return SearchCriteria.builder()
                .key(key)
                .operation(SearchOperator.EQUALS)
                .value(value)
                .build();
    }
    
    /**
     * LIKE 검색을 위한 생성자
     */
    public static SearchCriteria like(String key, String value) {
        return SearchCriteria.builder()
                .key(key)
                .operation(SearchOperator.LIKE)
                .value(value)
                .build();
    }
    
    /**
     * IN 검색을 위한 생성자
     */
    public static SearchCriteria in(String key, Object... values) {
        return SearchCriteria.builder()
                .key(key)
                .operation(SearchOperator.IN)
                .value(values)
                .build();
    }
    
    /**
     * BETWEEN 검색을 위한 생성자
     */
    public static SearchCriteria between(String key, Object start, Object end) {
        return SearchCriteria.builder()
                .key(key)
                .operation(SearchOperator.BETWEEN)
                .value(java.util.List.of(start, end))
                .build();
    }
    
    /**
     * NULL 체크를 위한 생성자
     */
    public static SearchCriteria isNull(String key) {
        return SearchCriteria.builder()
                .key(key)
                .operation(SearchOperator.IS_NULL)
                .value(null)
                .build();
    }
    
    /**
     * NOT NULL 체크를 위한 생성자
     */
    public static SearchCriteria isNotNull(String key) {
        return SearchCriteria.builder()
                .key(key)
                .operation(SearchOperator.IS_NOT_NULL)
                .value(null)
                .build();
    }
}