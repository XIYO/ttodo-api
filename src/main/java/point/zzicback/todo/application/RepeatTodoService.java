package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.*;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.domain.*;
import point.zzicback.todo.infrastructure.persistence.*;
import point.zzicback.member.application.MemberService;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepeatTodoService {
    private final RepeatTodoRepository repeatTodoRepository;
    private final TodoRepository todoRepository;
    private final MemberService memberService;
    
    @Transactional
    public void completeRepeatTodo(UUID memberId, Long originalTodoId, LocalDate completionDate) {
        Member member = memberService.findByIdOrThrow(memberId);
        
        Todo originalTodo = todoRepository.findByIdAndMemberId(originalTodoId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Todo", originalTodoId));
        
        if (todoRepository.existsByMemberIdAndDueDateAndOriginalTodoId(memberId, completionDate, originalTodoId)) {
            throw new BusinessException("이미 완료된 투두입니다");
        }
        
        Todo completedTodo = Todo.builder()
                .title(originalTodo.getTitle())
                .description(originalTodo.getDescription())
                .statusId(1)
                .priorityId(originalTodo.getPriorityId())
                .category(originalTodo.getCategory())
                .dueDate(completionDate)
                .dueTime(originalTodo.getDueTime())
                .originalTodoId(originalTodoId)
                .tags(new HashSet<>(originalTodo.getTags()))
                .member(member)
                .build();
        
        todoRepository.save(completedTodo);
    }
    
    @Transactional
    public void createRepeatTodo(Todo todo, Integer repeatType, Integer repeatInterval, 
                                LocalDate repeatStartDate, LocalDate repeatEndDate, Member member) {
        RepeatTodo repeatTodo = RepeatTodo.builder()
                .todo(todo)
                .repeatType(repeatType)
                .repeatInterval(repeatInterval != null ? repeatInterval : 1)
                .repeatStartDate(repeatStartDate)
                .repeatEndDate(repeatEndDate)
                .member(member)
                .build();
        
        repeatTodoRepository.save(repeatTodo);
    }
    
    public List<RepeatTodo> getActiveRepeatTodos(UUID memberId) {
        return repeatTodoRepository.findByMemberIdAndIsActiveTrue(memberId);
    }
    
    public RepeatTodo getRepeatTodoByTodoId(Long todoId) {
        return repeatTodoRepository.findByTodoId(todoId).orElse(null);
    }
    
    public List<LocalDate> generateVirtualDates(RepeatTodo repeatTodo, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = repeatTodo.getRepeatStartDate();
        
        while (!current.isAfter(endDate) && 
               (repeatTodo.getRepeatEndDate() == null || !current.isAfter(repeatTodo.getRepeatEndDate()))) {
            if (!current.isBefore(startDate)) {
                dates.add(current);
            }
            current = getNextDate(current, repeatTodo.getRepeatType(), repeatTodo.getRepeatInterval());
        }
        
        return dates;
    }
    
    private LocalDate getNextDate(LocalDate date, Integer repeatType, Integer interval) {
        return switch (repeatType) {
            case RepeatTypeConstants.DAILY -> date.plusDays(interval);
            case RepeatTypeConstants.WEEKLY -> date.plusWeeks(interval);
            case RepeatTypeConstants.MONTHLY -> date.plusMonths(interval);
            case RepeatTypeConstants.YEARLY -> date.plusYears(interval);
            default -> date.plusDays(1);
        };
    }
}