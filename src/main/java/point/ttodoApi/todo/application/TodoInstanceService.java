package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.common.error.EntityNotFoundException;
import point.ttodoApi.experience.application.event.*;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.todo.application.dto.command.*;
import point.ttodoApi.todo.application.dto.query.*;
import point.ttodoApi.todo.application.dto.result.*;
import point.ttodoApi.todo.application.mapper.TodoApplicationMapper;
import point.ttodoApi.todo.domain.*;
import point.ttodoApi.todo.infrastructure.persistence.*;
import point.ttodoApi.todo.domain.recurrence.RecurrenceEngine;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoInstanceService {
    
    private final TodoTemplateService todoTemplateService;
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final MemberService memberService;
    private final TodoApplicationMapper todoApplicationMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    public boolean existsVirtualTodo(UUID memberId, TodoId todoId) {
        return todoRepository.findByTodoIdAndOwnerId(todoId, memberId).isPresent();
    }
    
    public Page<TodoResult> getTodoList(TodoSearchQuery query) {
        // Specification을 사용한 동적 쿼리
        Specification<Todo> spec = TodoSpecification.createSpecification(
                query.memberId(),
                query.complete(),
                query.categoryIds(),
                query.priorityIds(),
                query.startDate(),
                query.endDate()
        );
        
        Page<Todo> todoPage = todoRepository.findAll(spec, Pageable.unpaged());
        return getTodoListWithVirtualTodos(query, todoPage);
    }
    
    public TodoResult getVirtualTodo(VirtualTodoQuery query) {
        TodoId todoId = new TodoId(query.originalTodoId(), query.daysDifference());
        
        // 먼저 Todo 테이블에서 확인 (active 상태 무관)
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(todoId, query.memberId());
        
        if (existingTodo.isPresent()) {
            Todo todo = existingTodo.get();
            // active=false인 경우만 404 에러 (삭제된 Todo)
            if (!todo.getActive()) {
                throw new EntityNotFoundException("Todo", query.originalTodoId() + ":" + query.daysDifference());
            }
            // Todo 테이블에 데이터가 있으면 반환 (완료/미완료 상관없이)
            return todoApplicationMapper.toResult(todo);
        }
        
        // Todo 테이블에 데이터가 없으면 가상 Todo 생성
        if (query.daysDifference() == 0) {
            // 원본 TodoTemplate 조회 (82:0)
            return todoTemplateService.getTodo(TodoQuery.of(query.memberId(), query.originalTodoId()));
        } else {
            // 가상 Todo 생성해서 반환
            TodoTemplate todoTemplate = todoTemplateService.getTodoTemplates(query.memberId())
                    .stream()
                    .filter(to -> to.getId().equals(query.originalTodoId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", query.originalTodoId()));
            
            LocalDate anchor = todoTemplate.getAnchorDate() != null ? todoTemplate.getAnchorDate() :
                    (todoTemplate.getDate() != null ? todoTemplate.getDate() : LocalDate.now());
            LocalDate targetDate = anchor.plusDays(query.daysDifference());
            
            String virtualId = query.originalTodoId() + ":" + query.daysDifference();
            return todoApplicationMapper.toVirtualResult(todoTemplate, virtualId, targetDate);
        }
    }

    @Transactional
    public void deactivateVirtualTodo(DeleteTodoCommand command) {
        TodoId todoId = new TodoId(command.originalTodoId(), command.daysDifference());
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(todoId, command.memberId());
        
        if (existingTodo.isPresent()) {
            // 이미 Todo 테이블에 데이터가 있으면 complete=true, active=false로 설정 (삭제 표시)
            Todo todo = existingTodo.get();
            todo.setComplete(true);
            todo.setActive(false);
            todoRepository.save(todo);
        } else {
            // Todo 테이블에 데이터가 없으면 새로 생성해서 complete=true, active=true로 설정
            List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(command.memberId());
            TodoTemplate todoTemplate = todoTemplates.stream()
                    .filter(to -> to.getId().equals(command.originalTodoId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", command.originalTodoId()));
            
            Member member = memberService.findByIdOrThrow(command.memberId());
            
            LocalDate anchor = todoTemplate.getAnchorDate() != null ? todoTemplate.getAnchorDate() :
                (todoTemplate.getDate() != null ? todoTemplate.getDate() : LocalDate.now());
            LocalDate targetDate = anchor.plusDays(command.daysDifference());
            
            Todo newTodo = Todo.builder()
                    .todoId(todoId)
                    .title(todoTemplate.getTitle())
                    .description(todoTemplate.getDescription())
                    .complete(true)  // 삭제 표시
                    .active(false)   // 비활성화
                    .priorityId(todoTemplate.getPriorityId())
                    .category(todoTemplate.getCategory())
                    .date(targetDate)
                    .time(todoTemplate.getTime())
                    .tags(new HashSet<>(todoTemplate.getTags()))
                    .owner(member)
                    .build();
            
            todoRepository.save(newTodo);
        }
    }
    
    @Transactional
    public TodoResult updateOrCreateVirtualTodo(UpdateVirtualTodoCommand command) {
        TodoId todoId = TodoId.fromVirtualId(command.virtualTodoId());
        Long originalTodoId = todoId.getId();
        Long daysDifference = todoId.getSeq();
        
        List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(command.memberId());
        TodoTemplate todoTemplate = todoTemplates.stream()
                .filter(to -> to.getId().equals(originalTodoId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", originalTodoId));
        
            LocalDate anchor = todoTemplate.getAnchorDate() != null ? todoTemplate.getAnchorDate() :
            (todoTemplate.getDate() != null ? todoTemplate.getDate() : LocalDate.now());
            LocalDate targetDate = anchor.plusDays(daysDifference);
        
        // active 상태에 관계없이 기존 Todo 확인
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(todoId, command.memberId());
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        if (existingTodo.isPresent()) {
            Todo todo = existingTodo.get();
            boolean wasIncomplete = !Boolean.TRUE.equals(todo.getComplete());
            boolean wasComplete = Boolean.TRUE.equals(todo.getComplete());
            
            // 삭제된 Todo를 다시 활성화
            todo.setActive(true);
            
            if (command.title() != null && !command.title().trim().isEmpty()) {
                todo.setTitle(command.title());
            }
            if (command.description() != null && !command.description().trim().isEmpty()) {
                todo.setDescription(command.description());
            }
            if (command.complete() != null) {
                todo.setComplete(command.complete());
            }
            if (command.priorityId() != null) {
                todo.setPriorityId(command.priorityId());
            }
            if (command.date() != null) {
                todo.setDate(command.date());
            }
            if (command.time() != null) {
                todo.setTime(command.time());
            }
            if (command.tags() != null && !command.tags().isEmpty()) {
                todo.setTags(command.tags());
            }
            
            // 카테고리 처리
            if (command.categoryId() != null) {
                Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.memberId())
                        .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
                todo.setCategory(category);
            }
            
            todoRepository.save(todo);
            
            // 투두 완료 시 경험치 이벤트 발생
            if (wasIncomplete && Boolean.TRUE.equals(todo.getComplete())) {
                eventPublisher.publishEvent(new TodoCompletedEvent(
                    command.memberId(),
                    originalTodoId,
                    todo.getTitle()
                ));
            }
            
            // 투두 완료 취소 시 경험치 차감 이벤트 발생
            if (wasComplete && Boolean.FALSE.equals(todo.getComplete())) {
                eventPublisher.publishEvent(new TodoUncompletedEvent(
                    command.memberId(),
                    originalTodoId,
                    todo.getTitle()
                ));
            }
            
            return todoApplicationMapper.toResult(todo);
        } else {
            // 새 Todo 생성
            Todo newTodo = Todo.builder()
                    .todoId(todoId)
                    .title(command.title() != null && !command.title().trim().isEmpty() ? command.title() : todoTemplate.getTitle())
                    .description(command.description() != null && !command.description().trim().isEmpty() ? command.description() : todoTemplate.getDescription())
                    .complete(command.complete() != null ? command.complete() : false)
                    .priorityId(command.priorityId() != null ? command.priorityId() : todoTemplate.getPriorityId())
                    .category(todoTemplate.getCategory())
                    .date(command.date() != null ? command.date() : targetDate)
                    .time(command.time() != null ? command.time() : todoTemplate.getTime())
                    .tags(command.tags() != null && !command.tags().isEmpty() ? command.tags() : new HashSet<>(todoTemplate.getTags()))
                    .owner(member)
                    .build();
            
            // 카테고리 변경이 있는 경우
            if (command.categoryId() != null) {
                Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.memberId())
                        .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
                newTodo.setCategory(category);
            }
            
            todoRepository.save(newTodo);
            
            // 새로 생성된 투두가 완료 상태인 경우 경험치 이벤트 발생
            if (Boolean.TRUE.equals(newTodo.getComplete())) {
                eventPublisher.publishEvent(new TodoCompletedEvent(
                    command.memberId(),
                    originalTodoId,
                    newTodo.getTitle()
                ));
            }
            
            return todoApplicationMapper.toResult(newTodo);
        }
    }
    
    public TodoStatistics getTodoStatistics(UUID memberId, LocalDate targetDate) {
        // 실제 Todo (DB에 저장된) 조회
        Specification<Todo> spec = TodoSpecification.createSpecification(
                memberId, null, null, null, targetDate, targetDate
        );
        Page<Todo> realTodoPage = todoRepository.findAll(spec, Pageable.unpaged());
        
        // TodoSearchQuery로 가상 투두 포함 전체 조회
        TodoSearchQuery query = new TodoSearchQuery(
                memberId,
                null, // complete - 모든 상태 조회
                null, null, null, null, targetDate,
                targetDate, // startDate = 대상 날짜
                targetDate, // endDate = 대상 날짜
                Pageable.unpaged()
        );

        // 실제 투두
        List<TodoResult> realTodoResults = realTodoPage.getContent().stream()
                .map(todoApplicationMapper::toResult)
                .toList();
        List<TodoResult> allTodos = new ArrayList<>(realTodoResults);
        
        // 가상 투두 (반복 투두 + 원본 투두)
        List<TodoResult> originalTodos = generateOriginalTodos(query);
        List<TodoResult> virtualTodos = generateVirtualTodos(query);
        allTodos.addAll(originalTodos);
        allTodos.addAll(virtualTodos);
        
        long total = allTodos.size();
        long completed = allTodos.stream()
                .mapToLong(todo -> Boolean.TRUE.equals(todo.complete()) ? 1 : 0)
                .sum();
        long inProgress = total - completed;
        
        return new TodoStatistics(total, inProgress, completed);
    }
    
    private Page<TodoResult> getTodoListWithVirtualTodos(TodoSearchQuery query, Page<Todo> todoPage) {
        List<TodoResult> realTodos = todoPage.getContent().stream()
                .filter(todo -> Boolean.TRUE.equals(todo.getActive()) && Boolean.TRUE.equals(todo.getComplete()))
                .map(todoApplicationMapper::toResult)
                .toList();
        
        List<TodoResult> originalTodos = generateOriginalTodos(query);
        List<TodoResult> virtualTodos = generateVirtualTodos(query);
        
        List<TodoResult> allTodos = new ArrayList<>();
        allTodos.addAll(realTodos);
        allTodos.addAll(originalTodos);
        allTodos.addAll(virtualTodos);
        
        if (query.pageable().getSort().isUnsorted()) {
            allTodos.sort(getDefaultComparator());
        }
        
        int start = (int) query.pageable().getOffset();
        int end = Math.min(start + query.pageable().getPageSize(), allTodos.size());
        List<TodoResult> pagedTodos = allTodos.subList(start, end);
        
        return new PageImpl<>(pagedTodos, query.pageable(), allTodos.size());
    }
    
    private List<TodoResult> generateVirtualTodos(TodoSearchQuery query) {
        if (query.startDate() == null || query.endDate() == null) {
            return new ArrayList<>();
        }
        
        // 완료만 조회하는 경우에만 가상 투두 제외
        if (query.complete() != null && query.complete()) {
            return new ArrayList<>();
        }
        
        List<TodoResult> virtualTodos = new ArrayList<>();
        LocalDate baseDate = query.date() != null ? query.date() : query.startDate();
        
        List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(query.memberId())
                .stream()
                .filter(to -> to.getRecurrenceRule() != null && to.getAnchorDate() != null)
                .filter(to -> matchesKeyword(to, query.keyword()))
                .toList();
        
        for (TodoTemplate todoTemplate : todoTemplates) {
            RecurrenceRule rule = todoTemplate.getRecurrenceRule();
            List<LocalDate> virtualDates = RecurrenceEngine.generateBetween(rule, query.startDate(), query.endDate());

            LocalDate originalDueDate = todoTemplate.getDate();

            for (LocalDate virtualDate : virtualDates) {
                // baseDate 이후의 가상 투두만 포함
                if (virtualDate.isBefore(baseDate)) {
                    continue;
                }

                if (virtualDate.equals(originalDueDate)) {
                    continue;
                }

                LocalDate anchor = todoTemplate.getAnchorDate() != null ? todoTemplate.getAnchorDate() : todoTemplate.getDate();
                long diff = anchor != null ? ChronoUnit.DAYS.between(anchor, virtualDate) : 0;
                Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(
                        new TodoId(todoTemplate.getId(), diff),
                        query.memberId());

                boolean isDeleted = existingTodo.isPresent() && Boolean.FALSE.equals(existingTodo.get().getActive());
                if (isDeleted) {
                    continue;
                }

                if (existingTodo.isEmpty() || !Boolean.TRUE.equals(existingTodo.get().getComplete())) {
                    long daysDifference = anchor != null ? ChronoUnit.DAYS.between(anchor, virtualDate) : 0;
                    String virtualId = todoTemplate.getId() + ":" + daysDifference;
                    virtualTodos.add(todoApplicationMapper.toVirtualResult(todoTemplate, virtualId, virtualDate));
                }
            }
        }

        return virtualTodos;
    }
    
    private List<TodoResult> generateOriginalTodos(TodoSearchQuery query) {
        // 완료만 조회하는 경우에만 원본 투두 제외 (이미 완료되어 실제 투두로 저장됨)
        if (query.complete() != null && query.complete()) {
            return new ArrayList<>();
        }
        
        List<TodoResult> originalTodos = new ArrayList<>();
        LocalDate baseDate = query.date() != null ? query.date() : query.startDate();
        
        List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(query.memberId())
                .stream()
                .filter(to -> matchesKeyword(to, query.keyword()))
                .filter(to -> matchesDateRange(to, query.startDate(), query.endDate()))
                .filter(to -> matchesCategoryFilter(to, query.categoryIds()))
                .filter(to -> matchesPriorityFilter(to, query.priorityIds()))
                .toList();
        
        for (TodoTemplate todoTemplate : todoTemplates) {
            // baseDate 이후의 원본 투두만 포함
            if (todoTemplate.getDate() != null && baseDate != null && todoTemplate.getDate().isBefore(baseDate)) {
                continue;
            }

            Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(
                    new TodoId(todoTemplate.getId(), 0L), query.memberId());

            boolean isDeleted = existingTodo.isPresent() && Boolean.FALSE.equals(existingTodo.get().getActive());
            if (isDeleted) {
                continue;
            }

            if (existingTodo.isEmpty() || !Boolean.TRUE.equals(existingTodo.get().getComplete())) {
                if (todoTemplate.getAnchorDate() != null && todoTemplate.getDate() != null) {
                    long daysDifference = ChronoUnit.DAYS.between(
                            todoTemplate.getAnchorDate(), todoTemplate.getDate());
                    String virtualId = todoTemplate.getId() + ":" + daysDifference;
                    originalTodos.add(todoApplicationMapper.toOriginalResult(todoTemplate, virtualId, todoTemplate.getDate()));
                } else {
                    String virtualId = todoTemplate.getId() + ":0";
                    originalTodos.add(todoApplicationMapper.toOriginalResult(todoTemplate, virtualId, todoTemplate.getDate()));
                }
            }
        }

        return originalTodos;
    }
    
    // (old repeat generation helpers removed; RRULE engine is the single source)

    
    private boolean matchesKeyword(TodoTemplate todoTemplate, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return (todoTemplate.getTitle() != null && todoTemplate.getTitle().toLowerCase().contains(lowerKeyword)) ||
               (todoTemplate.getDescription() != null && todoTemplate.getDescription().toLowerCase().contains(lowerKeyword)) ||
               (todoTemplate.getTags() != null && todoTemplate.getTags().stream()
                       .anyMatch(tag -> tag.toLowerCase().contains(lowerKeyword)));
    }
    
    private boolean matchesDateRange(TodoTemplate todoTemplate, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        
        LocalDate dueDate = todoTemplate.getDate();
        if (dueDate == null) {
            return true; // null인 경우는 항상 포함
        }
        
        if (startDate != null && dueDate.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && dueDate.isAfter(endDate)) {
            return false;
        }

        return true;
    }
    
    private boolean matchesCategoryFilter(TodoTemplate todoTemplate, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return true;
        }
        
        if (todoTemplate.getCategory() == null) {
            return categoryIds.contains(null);
        }
        
        return categoryIds.contains(todoTemplate.getCategory().getId());
    }
    
    private boolean matchesPriorityFilter(TodoTemplate todoTemplate, List<Integer> priorityIds) {
        if (priorityIds == null || priorityIds.isEmpty()) {
            return true;
        }
        
        return priorityIds.contains(todoTemplate.getPriorityId());
    }
    
    private Comparator<TodoResult> getDefaultComparator() {
        return Comparator
                .comparing((TodoResult t) -> t.date() != null ? t.date() : LocalDate.MAX)
                .thenComparing((TodoResult t) -> t.complete() != null ? t.complete() : false)
                .thenComparing((TodoResult t) -> t.isPinned() == null || !t.isPinned())
                .thenComparing((TodoResult t) -> t.displayOrder() != null ? t.displayOrder() : Integer.MAX_VALUE)
                .thenComparing((TodoResult t) -> t.priorityId() != null ? -t.priorityId() : Integer.MIN_VALUE)
                .thenComparing((TodoResult t) -> Long.parseLong(t.id().split(":")[0]));
    }
}
