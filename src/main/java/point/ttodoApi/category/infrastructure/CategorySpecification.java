package point.ttodoApi.category.infrastructure;

import org.springframework.stereotype.Component;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.common.specification.BaseSpecification;

import java.util.Set;

/**
 * Category 엔티티를 위한 동적 쿼리 Specification
 */
@Component
public class CategorySpecification extends BaseSpecification<Category> {
    
    // Category 엔티티에서 정렬 가능한 필드들
    private static final Set<String> CATEGORY_SORT_FIELDS = Set.of(
        "name",
        "color",
        "description",
        "owner.email",
        "owner.nickname"
    );
    
    @Override
    protected Set<String> getAllowedSortFields() {
        // 공통 필드와 Category 특화 필드를 합침
        Set<String> allowedFields = new java.util.HashSet<>(COMMON_SORT_FIELDS);
        allowedFields.addAll(CATEGORY_SORT_FIELDS);
        return allowedFields;
    }
}