package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.common.error.EntityNotFoundException;
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
public class TodoOriginalService {
    
    private final TodoOriginalRepository todoOriginalRepository;
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final MemberService memberService;
    private final TodoApplicationMapper todoApplicationMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    public TodoResult getTodo(TodoQuery query) {
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(query.todoId(), query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", query.todoId()));
        return todoApplicationMapper.toResult(todoOriginal);
    }
    
    @Transactional
    public void createTodo(CreateTodoCommand command) {
        command.validateRepeatDates();
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        Category category = null;
        if (command.categoryId() != null) {
            category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
                    .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
        }
        
        Integer repeatType = command.repeatType() != null ? command.repeatType() : 0;
        
        LocalDate repeatStartDate = command.repeatStartDate();
        if (repeatStartDate == null && repeatType > 0) {
            repeatStartDate = command.date();
        }
        
        TodoOriginal todoOriginal = TodoOriginal.builder()
                .title(command.title())
                .description(command.description())
                .priorityId(command.priorityId())
                .date(command.date())
                .time(command.time())
                .repeatType(repeatType)
                .repeatInterval(command.repeatInterval())
                .repeatStartDate(repeatStartDate)
                .repeatEndDate(command.repeatEndDate())
                .complete(command.complete())
                .daysOfWeek(command.daysOfWeek())
                .tags(command.tags())
                .category(category)
                .member(member)
                .build();
        
        todoOriginalRepository.save(todoOriginal);
    }
    
    @Transactional
    public void updateTodo(UpdateTodoCommand command) {
        command.validateRepeatDates();
        
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(command.todoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.todoId()));
        
        boolean wasIncomplete = !Boolean.TRUE.equals(todoOriginal.getComplete());
        boolean wasComplete = Boolean.TRUE.equals(todoOriginal.getComplete());
        
        Integer repeatType = command.repeatType() != null ? command.repeatType() : 0;
        
        todoOriginal.setTitle(command.title());
        todoOriginal.setDescription(command.description());
        todoOriginal.setPriorityId(command.priorityId());
        todoOriginal.setDate(command.date());
        todoOriginal.setTime(command.time());
        todoOriginal.setRepeatType(repeatType);
        todoOriginal.setRepeatInterval(command.repeatInterval());
        todoOriginal.setRepeatEndDate(command.repeatEndDate());
        todoOriginal.setDaysOfWeek(command.daysOfWeek());
        todoOriginal.setTags(command.tags());
        todoOriginal.setComplete(command.complete());
        
        if (command.repeatStartDate() != null) {
            todoOriginal.setRepeatStartDate(command.repeatStartDate());
        } else if (repeatType > 0 && command.date() != null) {
            todoOriginal.setRepeatStartDate(command.date());
        }
        
        // 투두 완료 시 경험치 이벤트 발생
        if (wasIncomplete && Boolean.TRUE.equals(todoOriginal.getComplete())) {
            eventPublisher.publishEvent(new TodoCompletedEvent(
                command.memberId(),
                command.todoId(),
                todoOriginal.getTitle()
            ));
        }
        
        // 투두 완료 취소 시 경험치 차감 이벤트 발생
        if (wasComplete && Boolean.FALSE.equals(todoOriginal.getComplete())) {
            eventPublisher.publishEvent(new TodoUncompletedEvent(
                command.memberId(),
                command.todoId(),
                todoOriginal.getTitle()
            ));
        }
    }
    
    @Transactional
    public void partialUpdateTodo(UpdateTodoCommand command) {
        command.validateRepeatDates();
        
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(command.todoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.todoId()));
        
        boolean wasIncomplete = !Boolean.TRUE.equals(todoOriginal.getComplete());
        boolean wasComplete = Boolean.TRUE.equals(todoOriginal.getComplete());
        
        if (command.title() != null && !command.title().trim().isEmpty()) {
            todoOriginal.setTitle(command.title());
        }
        if (command.description() != null && !command.description().trim().isEmpty()) {
            todoOriginal.setDescription(command.description());
        }
        if (command.priorityId() != null) {
            todoOriginal.setPriorityId(command.priorityId());
        }
        if (command.complete() != null) {
            todoOriginal.setComplete(command.complete());
        }
        if (command.date() != null) {
            todoOriginal.setDate(command.date());
        }
        if (command.time() != null) {
            todoOriginal.setTime(command.time());
        }
        if (command.repeatType() != null) {
            todoOriginal.setRepeatType(command.repeatType());
        }
        if (command.repeatInterval() != null) {
            todoOriginal.setRepeatInterval(command.repeatInterval());
        }
        if (command.repeatEndDate() != null) {
            todoOriginal.setRepeatEndDate(command.repeatEndDate());
        }
        if (command.daysOfWeek() != null && !command.daysOfWeek().isEmpty()) {
            todoOriginal.setDaysOfWeek(command.daysOfWeek());
        }
        if (command.tags() != null && !command.tags().isEmpty()) {
            todoOriginal.setTags(command.tags());
        }
        
        // 카테고리 처리
        if (command.categoryId() != null) {
            Category category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
                    .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
            todoOriginal.setCategory(category);
        }
        
        if (command.repeatStartDate() != null) {
            todoOriginal.setRepeatStartDate(command.repeatStartDate());
        }
        
        // 투두 완료 시 경험치 이벤트 발생
        if (wasIncomplete && Boolean.TRUE.equals(todoOriginal.getComplete())) {
            eventPublisher.publishEvent(new TodoCompletedEvent(
                command.memberId(),
                command.todoId(),
                todoOriginal.getTitle()
            ));
        }
        
        // 투두 완료 취소 시 경험치 차감 이벤트 발생
        if (wasComplete && Boolean.FALSE.equals(todoOriginal.getComplete())) {
            eventPublisher.publishEvent(new TodoUncompletedEvent(
                command.memberId(),
                command.todoId(),
                todoOriginal.getTitle()
            ));
        }
    }

    @Transactional
    public void deactivateTodo(DeleteTodoCommand command) {
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(command.originalTodoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.originalTodoId()));
        
        todoOriginal.setActive(false);
    }
    
    public List<TodoOriginal> getTodoOriginals(UUID memberId) {
        return todoOriginalRepository.findByMemberId(memberId);
    }
    
    public Page<String> getTags(UUID memberId, List<UUID> categoryIds, Pageable pageable) {
        return todoOriginalRepository.findDistinctTagsByMemberId(memberId, categoryIds, pageable);
    }
    
    /**
     * 사용자의 완료한 할일 개수 조회
     * @param memberId 회원 ID
     * @return 완료한 할일 개수
     */
    public long countCompletedTodos(UUID memberId) {
        return todoRepository.countCompletedTodosByMemberId(memberId);
    }
    
    @Transactional
    public void togglePin(TodoQuery query) {
        TodoOriginal todo = todoOriginalRepository.findByIdAndMemberId(query.todoId(), query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", query.todoId()));
        
        boolean wasPinned = todo.getIsPinned();
        todo.togglePin();
        
        if (!wasPinned && todo.getIsPinned()) {
            // 현재 투두를 제외한 기존 핀 고정 투두들을 조회
            List<TodoOriginal> existingPinnedTodos = todoOriginalRepository
                    .findByMemberIdAndIsPinnedTrueOrderByDisplayOrderAsc(query.memberId())
                    .stream()
                    .filter(t -> !t.getId().equals(todo.getId()))
                    .toList();
            
            // 새로 핀 고정되는 투두를 맨 뒤에 배치
            todo.setDisplayOrder(existingPinnedTodos.size());
        }
    }
    
    @Transactional
    public void changeOrder(UUID memberId, Long todoId, Integer newOrder) {
        TodoOriginal target = todoOriginalRepository.findByIdAndMemberId(todoId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", todoId));
        
        if (!target.getIsPinned()) {
            throw new IllegalArgumentException("Only pinned todos can be reordered");
        }
        
        List<TodoOriginal> pinnedTodos = todoOriginalRepository
                .findByMemberIdAndIsPinnedTrueOrderByDisplayOrderAsc(memberId);
        
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
     * 먼저 Todo 테이블을 확인하고, 없으면 TodoOriginal을 확인
     */
    public boolean isOwner(Long todoId, UUID memberId) {
        return todoOriginalRepository.findByIdAndMemberId(todoId, memberId).isPresent();
    }
    
    /**
     * Virtual Todo ID를 고려한 소유자 여부 확인 (Spring Security @PreAuthorize용)
     * 먼저 Todo 테이블을 확인하고, 없으면 TodoOriginal을 확인
     */
    public boolean isOwnerWithDaysDifference(Long originalTodoId, Long daysDifference, UUID memberId) {
        // 먼저 Virtual Todo 테이블 확인
        TodoId todoId = new TodoId(originalTodoId, daysDifference);
        boolean existsInTodoTable = todoRepository.findByTodoIdAndMemberId(todoId, memberId).isPresent();
        
        if (existsInTodoTable) {
            return true;
        }
        
        // Todo 테이블에 없으면 TodoOriginal 확인
        return todoOriginalRepository.findByIdAndMemberId(originalTodoId, memberId).isPresent();
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
     * TodoOriginal 권한 검증을 위한 엔티티 조회 (Spring Security @PreAuthorize용)
     * @param todoId 투두 ID
     * @return TodoOriginal 엔티티
     */
    public TodoOriginal findTodoOriginalForAuth(Long todoId) {
        return todoOriginalRepository.findById(todoId)
            .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", todoId));
    }
}
