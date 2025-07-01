package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.common.error.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.*;
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
    
    public TodoResult getTodo(TodoQuery query) {
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(query.todoId(), query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", query.todoId()));
        return toTodoResult(todoOriginal);
    }
    
    @Transactional
    public void createTodo(CreateTodoCommand command) {
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        Category category = null;
        if (command.categoryId() != null) {
            category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
                    .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
        }
        
        Integer repeatType = command.repeatType() != null ? command.repeatType() : 0;
        
        LocalDate repeatStartDate = command.repeatStartDate();
        if (repeatStartDate == null && repeatType > 0) {
            repeatStartDate = command.dueDate();
        }
        
        TodoOriginal todoOriginal = TodoOriginal.builder()
                .title(command.title())
                .description(command.description())
                .priorityId(command.priorityId())
                .dueDate(command.dueDate())
                .dueTime(command.dueTime())
                .repeatType(repeatType)
                .repeatInterval(command.repeatInterval())
                .repeatStartDate(repeatStartDate)
                .repeatEndDate(command.repeatEndDate())
                .statusId(0)
                .daysOfWeek(command.daysOfWeek())
                .tags(command.tags())
                .category(category)
                .member(member)
                .build();
        
        todoOriginalRepository.save(todoOriginal);
    }
    
    @Transactional
    public void updateTodo(UpdateTodoCommand command) {
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(command.todoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.todoId()));
        
        Integer repeatType = command.repeatType() != null ? command.repeatType() : 0;
        
        todoOriginal.setTitle(command.title());
        todoOriginal.setDescription(command.description());
        todoOriginal.setPriorityId(command.priorityId());
        todoOriginal.setDueDate(command.dueDate());
        todoOriginal.setDueTime(command.dueTime());
        todoOriginal.setRepeatType(repeatType);
        todoOriginal.setRepeatInterval(command.repeatInterval());
        todoOriginal.setRepeatEndDate(command.repeatEndDate());
        todoOriginal.setDaysOfWeek(command.daysOfWeek());
        todoOriginal.setTags(command.tags());
        
        if (command.repeatStartDate() != null) {
            todoOriginal.setRepeatStartDate(command.repeatStartDate());
        } else if (repeatType > 0 && command.dueDate() != null) {
            todoOriginal.setRepeatStartDate(command.dueDate());
        }
    }
    
    @Transactional
    public void partialUpdateTodo(UpdateTodoCommand command) {
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(command.todoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.todoId()));
        
        if (command.title() != null && !command.title().trim().isEmpty()) {
            todoOriginal.setTitle(command.title());
        }
        if (command.description() != null && !command.description().trim().isEmpty()) {
            todoOriginal.setDescription(command.description());
        }
        if (command.priorityId() != null) {
            todoOriginal.setPriorityId(command.priorityId());
        }
        if (command.statusId() != null) {
            todoOriginal.setStatusId(command.statusId());
        }
        if (command.dueDate() != null) {
            todoOriginal.setDueDate(command.dueDate());
        }
        if (command.dueTime() != null) {
            todoOriginal.setDueTime(command.dueTime());
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
    }
    
    @Transactional
    public void deleteTodo(TodoQuery query) {
        TodoOriginal todoOriginal = todoOriginalRepository.findByIdAndMemberId(query.todoId(), query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", query.todoId()));
        todoOriginalRepository.delete(todoOriginal);
    }
    
    public List<TodoOriginal> getActiveTodoOriginals(UUID memberId) {
        return todoOriginalRepository.findByMemberIdAndIsActiveTrue(memberId);
    }
    
    public Page<String> getTags(UUID memberId, List<Long> categoryIds, Pageable pageable) {
        return todoOriginalRepository.findDistinctTagsByMemberId(memberId, categoryIds, pageable);
    }
    
    private TodoResult toTodoResult(TodoOriginal todoOriginal) {
        // 반복이 없는 투두들도 가상 ID로 표시 (originalId:0 형태)
        long daysDifference = 0;
        String virtualId = todoOriginal.getId() + ":" + daysDifference;
        
        String statusName = switch (todoOriginal.getStatusId()) {
            case 0 -> "진행중";
            case 1 -> "완료";
            case 2 -> "지연";
            default -> "알 수 없음";
        };
        
        String priorityName = null;
        if (todoOriginal.getPriorityId() != null) {
            priorityName = switch (todoOriginal.getPriorityId()) {
                case 0 -> "낮음";
                case 1 -> "보통";
                case 2 -> "높음";
                default -> "알 수 없음";
            };
        }
        
        return new TodoResult(
                virtualId,
                todoOriginal.getTitle(),
                todoOriginal.getDescription(),
                todoOriginal.getStatusId(),
                statusName,
                todoOriginal.getPriorityId(),
                priorityName,
                todoOriginal.getCategory() != null ? todoOriginal.getCategory().getId() : null,
                todoOriginal.getCategory() != null ? todoOriginal.getCategory().getName() : null,
                todoOriginal.getDueDate(),
                todoOriginal.getDueTime(),
                todoOriginal.getRepeatType(),
                todoOriginal.getRepeatInterval(),
                todoOriginal.getRepeatEndDate(),
                todoOriginal.getDaysOfWeek(),
                todoOriginal.getId(),
                todoOriginal.getTags()
        );
    }
}
