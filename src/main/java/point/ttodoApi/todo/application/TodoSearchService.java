package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.shared.specification.*;
import point.ttodoApi.todo.domain.Todo;
import point.ttodoApi.todo.dto.request.TodoSearchRequest;
import point.ttodoApi.todo.infrastructure.persistence.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 동적 쿼리를 사용한 Todo 검색 서비스 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoSearchService {
    
    private final TodoRepository todoRepository;
    private final TodoSpecificationV2 todoSpecification;
    private final SortValidator sortValidator;
    
    /**
     * 동적 검색 예제 - 다양한 조건으로 Todo 검색
     */
    public Page<Todo> searchTodos(TodoSearchRequest request, Pageable pageable) {
        // 정렬 필드 검증
        sortValidator.validateSort(pageable.getSort(), todoSpecification);
        
        // SpecificationBuilder를 사용한 동적 쿼리 구성
        SpecificationBuilder<Todo> builder = new SpecificationBuilder<>(todoSpecification);
        
        Specification<Todo> spec = builder
                // 필수 조건
                .with("member.id", request.getMemberId())
                .with("active", true)
                
                // 선택적 조건들
                .withIf(request.getComplete() != null, "complete", request.getComplete())
                .withLike("title", request.getKeyword())
                .withIn("category.id", request.getCategoryIds())
                .withIn("priorityId", request.getPriorityIds())
                .withDateRange("date", request.getStartDate(), request.getEndDate())
                
                // 복잡한 조건 예제
                .withIf(request.isUrgentOnly(), builder2 -> 
                    builder2.with("priorityId", 1)
                           .withBetween("date", LocalDate.now(), LocalDate.now().plusDays(3)))
                
                .build();
        
        return todoRepository.findAll(spec, pageable);
    }
    
    
    /**
     * 카테고리별 Todo 개수 조회
     */
    public long countTodosByCategory(UUID memberId, UUID categoryId) {
        SpecificationBuilder<Todo> builder = new SpecificationBuilder<>(todoSpecification);
        
        Specification<Todo> spec = builder
                .with("member.id", memberId)
                .with("active", true)
                .with("category.id", categoryId)
                .build();
        
        return todoRepository.count(spec);
    }
    
}