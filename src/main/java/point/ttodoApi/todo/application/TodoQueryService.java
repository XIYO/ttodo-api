package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.todo.application.mapper.TodoApplicationMapper;
import point.ttodoApi.todo.application.query.*;
import point.ttodoApi.todo.application.result.TodoResult;
import point.ttodoApi.todo.domain.Todo;
import point.ttodoApi.todo.domain.TodoId;
import point.ttodoApi.todo.infrastructure.persistence.TodoRepository;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Todo Query Service
 * TTODO 아키텍처 패턴: Query(읽기) 처리 전용 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class TodoQueryService {

    private final TodoRepository todoRepository;
    private final TodoApplicationMapper mapper;

    /**
     * Todo 검색 - 단순한 Specification 사용
     */
    public Page<TodoResult> searchTodos(@Valid TodoSearchQuery query, Pageable pageable) {
        log.debug("Searching todos with query: {}", query);
        
        // Modern conditional Specification building approach
        Specification<Todo> spec = (root, q, cb) -> cb.conjunction();
        
        // 필수 조건
        if (query.userId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("owner").get("id"), query.userId()));
        }
        spec = spec.and((root, q, cb) -> cb.isTrue(root.get("active")));
        
        // 선택적 조건들
        if (query.keyword() != null) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("title")), "%" + query.keyword().toLowerCase() + "%"));
        }
        if (query.complete() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("complete"), query.complete()));
        }
        if (query.categoryIds() != null && !query.categoryIds().isEmpty()) {
            spec = spec.and((root, q, cb) -> root.get("category").get("id").in(query.categoryIds()));
        }

        Page<Todo> todos = todoRepository.findAll(spec, pageable);
        return todos.map(mapper::toResult);
    }

    /**
     * 단일 Todo 조회
     */
    public Optional<TodoResult> getTodo(@Valid TodoQuery query) {
        log.debug("Getting todo with query: {}", query);
        
        // TodoId 생성 (일반적으로 seq는 0)
        TodoId todoId = new TodoId(query.todoId(), 0L);
        
        return todoRepository.findByTodoIdAndOwnerId(todoId, query.userId())
            .map(mapper::toResult);
    }

    /**
     * 회원의 모든 Todo 조회
     */
    public List<TodoResult> getTodosByUser(UUID userId) {
        log.debug("Getting todos for user: {}", userId);
        
        List<Todo> todos = todoRepository.findAccessibleTodosByuserId(userId);
        return todos.stream()
            .map(mapper::toResult)
            .toList();
    }

    /**
     * 캘린더 용 Todo 조회
     */
    public List<TodoResult> getCalendarTodos(@Valid CalendarQuery query) {
        log.debug("Getting calendar todos with query: {}", query);
        
        Specification<Todo> spec = (root, q, cb) -> cb.conjunction();
        
        // 필수 조건
        if (query.userId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("owner").get("id"), query.userId()));
        }
        spec = spec.and((root, q, cb) -> cb.isTrue(root.get("active")));
        
        // 월별 날짜 범위 조건 (year, month 기반)
        LocalDate startOfMonth = LocalDate.of(query.year(), query.month(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        
        spec = spec.and((root, q, cb) -> 
            cb.or(
                cb.isNull(root.get("date")),
                cb.between(root.get("date"), startOfMonth, endOfMonth)
            )
        );

        List<Todo> todos = todoRepository.findAll(spec);
        return todos.stream()
            .map(mapper::toResult)
            .toList();
    }
}