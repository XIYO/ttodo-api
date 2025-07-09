package point.ttodoApi.todo.infrastructure.persistence;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import point.ttodoApi.todo.domain.Todo;

import java.time.LocalDate;
import java.util.*;

public class TodoSpecification {
    
    public static Specification<Todo> createSpecification(UUID memberId, Boolean complete, 
                                                         List<Long> categoryIds, List<Integer> priorityIds,
                                                         LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 필수 조건들
            predicates.add(criteriaBuilder.equal(root.get("member").get("id"), memberId));
            predicates.add(criteriaBuilder.isTrue(root.get("active")));
            
            // 완료 여부 필터
            if (complete != null) {
                predicates.add(criteriaBuilder.equal(root.get("complete"), complete));
            }
            
            // 카테고리 필터
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            
            // 우선순위 필터
            if (priorityIds != null && !priorityIds.isEmpty()) {
                predicates.add(root.get("priorityId").in(priorityIds));
            }
            
            // 날짜 필터
            if (startDate != null) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("date")),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate)
                ));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("date")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate)
                ));
            }
            
            // 정렬 설정
            if (query != null) {
                query.orderBy(
                    // 날짜/시간이 없는 항목을 뒤로
                    criteriaBuilder.asc(
                        criteriaBuilder.selectCase()
                            .when(criteriaBuilder.and(
                                criteriaBuilder.isNull(root.get("date")),
                                criteriaBuilder.isNull(root.get("time"))
                            ), 1)
                            .otherwise(0)
                    ),
                    // 미완료 항목을 먼저
                    criteriaBuilder.asc(
                        criteriaBuilder.selectCase()
                            .when(criteriaBuilder.isFalse(root.get("complete")), 0)
                            .otherwise(1)
                    ),
                    // 날짜 오름차순
                    criteriaBuilder.asc(root.get("date")),
                    // 시간 오름차순
                    criteriaBuilder.asc(root.get("time")),
                    // 생성일 오름차순
                    criteriaBuilder.asc(root.get("createdAt"))
                );
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}