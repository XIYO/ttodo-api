package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.stereotype.Component;
import point.ttodoApi.common.specification.BaseSpecification;
import point.ttodoApi.todo.domain.Todo;

import java.util.Set;

/**
 * Todo 엔티티를 위한 동적 쿼리 Specification
 */
@Component
public class TodoSpecificationV2 extends BaseSpecification<Todo> {
    
    // Todo 엔티티에서 정렬 가능한 필드들
    private static final Set<String> TODO_SORT_FIELDS = Set.of(
        "title",
        "date",
        "time",
        "priorityId",
        "complete",
        "active",
        "displayOrder",
        "member.email",
        "member.nickname",
        "category.name"
    );
    
    @Override
    protected Set<String> getAllowedSortFields() {
        // 공통 필드와 Todo 특화 필드를 합침
        Set<String> allowedFields = new java.util.HashSet<>(COMMON_SORT_FIELDS);
        allowedFields.addAll(TODO_SORT_FIELDS);
        return allowedFields;
    }
}