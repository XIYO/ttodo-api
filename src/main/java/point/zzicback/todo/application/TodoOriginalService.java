package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.common.error.*;
import point.zzicback.experience.application.event.TodoCompletedEvent;
import point.zzicback.experience.application.event.TodoUncompletedEvent;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.*;
import point.zzicback.todo.application.mapper.TodoApplicationMapper;
import point.zzicback.todo.domain.*;
import point.zzicback.todo.infrastructure.persistence.*;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoOriginalService {
    
    private final TodoOriginalRepository todoOriginalRepository;
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
    public void deleteTodo(TodoQuery query) {
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(query.todoId(), query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", query.todoId()));
        todoOriginalRepository.delete(todoOriginal);
    }
    
    public List<TodoOriginal> getTodoOriginals(UUID memberId) {
        return todoOriginalRepository.findByMemberId(memberId);
    }
    
    public Page<String> getTags(UUID memberId, List<Long> categoryIds, Pageable pageable) {
        return todoOriginalRepository.findDistinctTagsByMemberId(memberId, categoryIds, pageable);
    }
}
