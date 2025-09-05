package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.shared.error.EntityNotFoundException;
import point.ttodoApi.experience.application.event.*;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.todo.application.dto.command.*;
import point.ttodoApi.todo.application.dto.query.TodoQuery;
import point.ttodoApi.todo.application.dto.result.TodoResult;
import point.ttodoApi.todo.application.mapper.TodoApplicationMapper;
import point.ttodoApi.todo.domain.*;
import point.ttodoApi.todo.infrastructure.persistence.*;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoTemplateService {
    
    private final TodoTemplateRepository todoTemplateRepository;
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final MemberService memberService;
    private final TodoApplicationMapper todoApplicationMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    public TodoResult getTodo(TodoQuery query) {
        TodoTemplate todoTemplate = todoTemplateRepository.findByIdAndOwnerId(query.todoId(), query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", query.todoId()));
        return todoApplicationMapper.toResult(todoTemplate);
    }
    
    @Transactional
    public void createTodo(CreateTodoCommand command) {
        command.validateRule();
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        Category category = null;
        if (command.categoryId() != null) {
            category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.memberId())
                    .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
        }
        
        TodoTemplate todoTemplate = TodoTemplate.builder()
                .title(command.title())
                .description(command.description())
                .priorityId(command.priorityId())
                .date(command.date())
                .time(command.time())
                // 신규 규칙 저장(있으면 우선 사용)
                .complete(command.complete())
                .tags(command.tags())
                .category(category)
                .owner(member)
                .build();

        if (command.recurrenceRule() != null) {
            todoTemplate.setRecurrenceRule(command.recurrenceRule());
            // 앵커가 명시되지 않았다면 date를 기본으로 설정
            LocalDate anchor = command.recurrenceRule().getAnchorDate();
            if (anchor == null) anchor = command.date();
            todoTemplate.setAnchorDate(anchor);
        }
        
        todoTemplateRepository.save(todoTemplate);
    }
    
    @Transactional
    public void updateTodo(UpdateTodoCommand command) {
        command.validateRule();
        
        TodoTemplate todoTemplate = todoTemplateRepository.findByIdAndOwnerId(command.todoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", command.todoId()));
        
        boolean wasIncomplete = !Boolean.TRUE.equals(todoTemplate.getComplete());
        boolean wasComplete = Boolean.TRUE.equals(todoTemplate.getComplete());
        
        todoTemplate.setTitle(command.title());
        todoTemplate.setDescription(command.description());
        todoTemplate.setPriorityId(command.priorityId());
        todoTemplate.setDate(command.date());
        todoTemplate.setTime(command.time());
        todoTemplate.setTags(command.tags());
        todoTemplate.setComplete(command.complete());
        
        if (command.recurrenceRule() != null) {
            todoTemplate.setRecurrenceRule(command.recurrenceRule());
            LocalDate anchor = command.recurrenceRule().getAnchorDate();
            if (anchor == null) anchor = todoTemplate.getDate();
            todoTemplate.setAnchorDate(anchor);
        }
        
        // 투두 완료 시 경험치 이벤트 발생
        if (wasIncomplete && Boolean.TRUE.equals(todoTemplate.getComplete())) {
            eventPublisher.publishEvent(new TodoCompletedEvent(
                command.memberId(),
                command.todoId(),
                todoTemplate.getTitle()
            ));
        }
        
        // 투두 완료 취소 시 경험치 차감 이벤트 발생
        if (wasComplete && Boolean.FALSE.equals(todoTemplate.getComplete())) {
            eventPublisher.publishEvent(new TodoUncompletedEvent(
                command.memberId(),
                command.todoId(),
                todoTemplate.getTitle()
            ));
        }
    }
    
    @Transactional
    public void partialUpdateTodo(UpdateTodoCommand command) {
        command.validateRule();
        
        TodoTemplate todoTemplate = todoTemplateRepository.findByIdAndOwnerId(command.todoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", command.todoId()));
        
        boolean wasIncomplete = !Boolean.TRUE.equals(todoTemplate.getComplete());
        boolean wasComplete = Boolean.TRUE.equals(todoTemplate.getComplete());
        
        if (command.title() != null && !command.title().trim().isEmpty()) {
            todoTemplate.setTitle(command.title());
        }
        if (command.description() != null && !command.description().trim().isEmpty()) {
            todoTemplate.setDescription(command.description());
        }
        if (command.priorityId() != null) {
            todoTemplate.setPriorityId(command.priorityId());
        }
        if (command.complete() != null) {
            todoTemplate.setComplete(command.complete());
        }
        if (command.date() != null) {
            todoTemplate.setDate(command.date());
        }
        if (command.time() != null) {
            todoTemplate.setTime(command.time());
        }
        // 반복 규칙 업데이트
        if (command.recurrenceRule() != null) {
            todoTemplate.setRecurrenceRule(command.recurrenceRule());
            LocalDate anchor = command.recurrenceRule().getAnchorDate();
            if (anchor == null) anchor = todoTemplate.getDate();
            todoTemplate.setAnchorDate(anchor);
        }
        if (command.tags() != null && !command.tags().isEmpty()) {
            todoTemplate.setTags(command.tags());
        }
        
        // 카테고리 처리
        if (command.categoryId() != null) {
            Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.memberId())
                    .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
            todoTemplate.setCategory(category);
        }
        
        
        
        // 투두 완료 시 경험치 이벤트 발생
        if (wasIncomplete && Boolean.TRUE.equals(todoTemplate.getComplete())) {
            eventPublisher.publishEvent(new TodoCompletedEvent(
                command.memberId(),
                command.todoId(),
                todoTemplate.getTitle()
            ));
        }
        
        // 투두 완료 취소 시 경험치 차감 이벤트 발생
        if (wasComplete && Boolean.FALSE.equals(todoTemplate.getComplete())) {
            eventPublisher.publishEvent(new TodoUncompletedEvent(
                command.memberId(),
                command.todoId(),
                todoTemplate.getTitle()
            ));
        }
    }

    @Transactional
    public void deactivateTodo(DeleteTodoCommand command) {
        TodoTemplate todoTemplate = todoTemplateRepository.findByIdAndOwnerId(command.originalTodoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", command.originalTodoId()));
        
        todoTemplate.setActive(false);
    }
    
    public List<TodoTemplate> getTodoTemplates(UUID memberId) {
        return todoTemplateRepository.findByOwnerId(memberId);
    }
    
    public Page<String> getTags(UUID memberId, List<UUID> categoryIds, Pageable pageable) {
        return todoTemplateRepository.findDistinctTagsByOwnerId(memberId, categoryIds, pageable);
    }
    
    /**
     * 사용자의 완료한 할일 개수 조회
     * @param memberId 회원 ID
     * @return 완료한 할일 개수
     */
    public long countCompletedTodos(UUID memberId) {
        return todoRepository.countCompletedTodosByOwnerId(memberId);
    }
    
    @Transactional
    public void togglePin(TodoQuery query) {
        TodoTemplate todo = todoTemplateRepository.findByIdAndOwnerId(query.todoId(), query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", query.todoId()));
        
        boolean wasPinned = todo.getIsPinned();
        todo.togglePin();
        
        if (!wasPinned && todo.getIsPinned()) {
            // 현재 투두를 제외한 기존 핀 고정 투두들을 조회
            List<TodoTemplate> existingPinnedTodos = todoTemplateRepository
                    .findByOwnerIdAndIsPinnedTrueOrderByDisplayOrderAsc(query.memberId())
                    .stream()
                    .filter(t -> !t.getId().equals(todo.getId()))
                    .toList();
            
            // 새로 핀 고정되는 투두를 맨 뒤에 배치
            todo.setDisplayOrder(existingPinnedTodos.size());
        }
    }
    
    @Transactional
    public void changeOrder(UUID memberId, Long todoId, Integer newOrder) {
        TodoTemplate target = todoTemplateRepository.findByIdAndOwnerId(todoId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", todoId));
        
        if (!target.getIsPinned()) {
            throw new IllegalArgumentException("Only pinned todos can be reordered");
        }
        
        List<TodoTemplate> pinnedTodos = todoTemplateRepository
                .findByOwnerIdAndIsPinnedTrueOrderByDisplayOrderAsc(memberId);
        
        if (newOrder < 0 || newOrder >= pinnedTodos.size()) {
            throw new IllegalArgumentException("Invalid order index");
        }
        
        pinnedTodos.removeIf(todo -> todo.getId().equals(todoId));
        pinnedTodos.add(newOrder, target);
        
        for (int i = 0; i < pinnedTodos.size(); i++) {
            pinnedTodos.get(i).setDisplayOrder(i);
        }
    }
    
    /**
     * Todo 소유자 여부 확인 (Spring Security @PreAuthorize용)
     * 먼저 Todo 테이블을 확인하고, 없으면 TodoTemplate을 확인
     */
    public boolean isOwner(Long todoId, UUID memberId) {
        return todoTemplateRepository.findByIdAndOwnerId(todoId, memberId).isPresent();
    }
    
    /**
     * Virtual Todo ID를 고려한 소유자 여부 확인 (Spring Security @PreAuthorize용)
     * 먼저 Todo 테이블을 확인하고, 없으면 TodoTemplate을 확인
     */
    public boolean isOwnerWithDaysDifference(Long originalTodoId, Long daysDifference, UUID memberId) {
        // 먼저 Virtual Todo 테이블 확인
        TodoId todoId = new TodoId(originalTodoId, daysDifference);
        boolean existsInTodoTable = todoRepository.findByTodoIdAndOwnerId(todoId, memberId).isPresent();
        
        if (existsInTodoTable) {
            return true;
        }
        
        // Todo 테이블에 없으면 TodoTemplate 확인
        return todoTemplateRepository.findByIdAndOwnerId(originalTodoId, memberId).isPresent();
    }
    
    /**
     * 태그 조회 권한 확인 (Spring Security @PreAuthorize용)
     * 인증된 사용자는 자신의 태그만 조회 가능
     */
    public boolean canAccessTags(UUID memberId) {
        // 인증된 사용자라면 자신의 태그에는 항상 접근 가능
        return memberId != null;
    }
    
    /**
     * TodoTemplate 권한 검증을 위한 엔티티 조회 (Spring Security @PreAuthorize용)
     * @param todoId 투두 ID
     * @return TodoTemplate 엔티티
     */
    public TodoTemplate findTodoTemplateForAuth(Long todoId) {
        return todoTemplateRepository.findById(todoId)
            .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", todoId));
    }
}
