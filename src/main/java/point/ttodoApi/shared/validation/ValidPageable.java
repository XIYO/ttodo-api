package point.ttodoApi.shared.validation;

import java.lang.annotation.*;

/**
 * Pageable 파라미터 검증을 위한 어노테이션
 * 이 어노테이션이 붙은 메서드의 Pageable 파라미터는 자동으로 검증됨
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidPageable {
    
    /**
     * 허용할 정렬 필드 제공자
     * 기본값은 COMMON (id, createdAt, updatedAt만 허용)
     */
    SortFieldsProvider sortFields() default SortFieldsProvider.COMMON;
    
    /**
     * 최대 페이지 크기 (기본값: 100)
     * PageableConfig의 전역 설정을 오버라이드할 때 사용
     */
    int maxPageSize() default 100;
}