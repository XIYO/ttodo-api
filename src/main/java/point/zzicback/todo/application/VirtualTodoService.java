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
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VirtualTodoService {
    
    private final TodoOriginalService todoOriginalService;
    private final TodoRepository todoRepository;
    private final TodoOriginalRepository todoOriginalRepository;
    private final CategoryRepository categoryRepository;
    private final MemberService memberService;
    
    public Page<TodoResult> getTodoList(TodoSearchQuery query) {
        Page<Todo> todoPage = todoRepository.findByMemberId(
                query.memberId(),
                query.categoryIds(),
                query.statusIds(),
                query.priorityIds(),
                query.hideStatusIds(),
                query.startDate(),
                query.endDate(),
                query.pageable());
        
        return getTodoListWithVirtualTodos(query, todoPage);
    }
    
    @Transactional
    public TodoResult completeVirtualTodo(CompleteVirtualTodoCommand command) {
        List<TodoOriginal> todoOriginals = todoOriginalService.getActiveTodoOriginals(command.memberId());
        TodoOriginal todoOriginal = todoOriginals.stream()
                .filter(to -> to.getId().equals(command.originalTodoId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.originalTodoId()));
        
        LocalDate completionDate = todoOriginal.getDueDate().plusDays(command.daysDifference());
        
        // 복합키 생성
        TodoId todoId = new TodoId(command.originalTodoId(), command.daysDifference());
        
        // 중복 완료 체크
        if (todoRepository.existsByMemberIdAndDueDateAndOriginalTodoId(
                command.memberId(), completionDate, command.originalTodoId())) {
            throw new BusinessException("이미 완료된 투두입니다");
        }
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        // 새로운 완료된 투두 생성
        Todo completedTodo = Todo.builder()
                .todoId(todoId) // 복합키 설정 (82, 1)
                .title(todoOriginal.getTitle())
                .description(todoOriginal.getDescription())
                .statusId(1)
                .priorityId(todoOriginal.getPriorityId())
                .category(todoOriginal.getCategory())
                .dueDate(completionDate)
                .dueTime(todoOriginal.getDueTime())
                .tags(new HashSet<>(todoOriginal.getTags()))
                .member(member)
                .build();
        
        todoRepository.save(completedTodo);
        
        return toTodoResult(completedTodo);
    }
    
    @Transactional
    public void deleteRepeatTodo(DeleteRepeatTodoCommand command) {
        List<TodoOriginal> todoOriginals = todoOriginalService.getActiveTodoOriginals(command.memberId());
        TodoOriginal todoOriginal = todoOriginals.stream()
                .filter(to -> to.getId().equals(command.originalTodoId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.originalTodoId()));
        
        LocalDate stopDate = todoOriginal.getDueDate().plusDays(command.daysDifference());
        LocalDate newEndDate = stopDate.minusDays(1);
        
        if (todoOriginal.getRepeatEndDate() != null && 
            todoOriginal.getRepeatEndDate().isBefore(newEndDate)) {
            return;
        }
        
        todoOriginal.setRepeatEndDate(newEndDate);
    }
    
    @Transactional
    public TodoResult updateOrCreateVirtualTodo(UpdateVirtualTodoCommand command) {
        TodoId todoId = TodoId.fromVirtualId(command.virtualTodoId());
        Long originalTodoId = todoId.getId();
        Long daysDifference = todoId.getSeq();
        
        // 원본 Todo 조회
        List<TodoOriginal> todoOriginals = todoOriginalService.getActiveTodoOriginals(command.memberId());
        TodoOriginal todoOriginal = todoOriginals.stream()
                .filter(to -> to.getId().equals(originalTodoId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", originalTodoId));
        
        LocalDate targetDate = todoOriginal.getDueDate().plusDays(daysDifference);
        
        // 기존 완료된 Todo가 있는지 확인
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndMemberId(todoId, command.memberId());
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        if (existingTodo.isPresent()) {
            // 기존 Todo 업데이트
            Todo todo = existingTodo.get();
            
            if (command.title() != null && !command.title().trim().isEmpty()) {
                todo.setTitle(command.title());
            }
            if (command.description() != null && !command.description().trim().isEmpty()) {
                todo.setDescription(command.description());
            }
            if (command.statusId() != null) {
                todo.setStatusId(command.statusId());
            }
            if (command.priorityId() != null) {
                todo.setPriorityId(command.priorityId());
            }
            if (command.dueDate() != null) {
                todo.setDueDate(command.dueDate());
            }
            if (command.dueTime() != null) {
                todo.setDueTime(command.dueTime());
            }
            if (command.tags() != null && !command.tags().isEmpty()) {
                todo.setTags(command.tags());
            }
            
            // 카테고리 처리
            if (command.categoryId() != null) {
                Category category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
                        .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
                todo.setCategory(category);
            }
            
            todoRepository.save(todo);
            return toTodoResult(todo);
        } else {
            // 새 Todo 생성
            Todo newTodo = Todo.builder()
                    .todoId(todoId)
                    .title(command.title() != null && !command.title().trim().isEmpty() ? command.title() : todoOriginal.getTitle())
                    .description(command.description() != null && !command.description().trim().isEmpty() ? command.description() : todoOriginal.getDescription())
                    .statusId(command.statusId() != null ? command.statusId() : 1) // 기본값은 완료
                    .priorityId(command.priorityId() != null ? command.priorityId() : todoOriginal.getPriorityId())
                    .category(todoOriginal.getCategory())
                    .dueDate(command.dueDate() != null ? command.dueDate() : targetDate)
                    .dueTime(command.dueTime() != null ? command.dueTime() : todoOriginal.getDueTime())
                    .tags(command.tags() != null && !command.tags().isEmpty() ? command.tags() : new HashSet<>(todoOriginal.getTags()))
                    .member(member)
                    .build();
            
            // 카테고리 변경이 있는 경우
            if (command.categoryId() != null) {
                Category category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
                        .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
                newTodo.setCategory(category);
            }
            
            todoRepository.save(newTodo);
            return toTodoResult(newTodo);
        }
    }
    
    private Page<TodoResult> getTodoListWithVirtualTodos(TodoSearchQuery query, Page<Todo> todoPage) {
        List<TodoResult> realTodos = todoPage.getContent().stream()
                .map(this::toTodoResult)
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
        
        if (query.hideStatusIds() != null && query.hideStatusIds().contains(0)) {
            return new ArrayList<>();
        }
        
        if (query.statusIds() != null && !query.statusIds().isEmpty() && 
            !query.statusIds().contains(0)) {
            return new ArrayList<>();
        }
        
        List<TodoResult> virtualTodos = new ArrayList<>();
        LocalDate baseDate = query.date() != null ? query.date() : query.startDate();
        
        List<TodoOriginal> todoOriginals = todoOriginalService.getActiveTodoOriginals(query.memberId())
                .stream()
                .filter(to -> to.getRepeatStartDate() != null) // repeat_start_date가 null이면 반복 사용 안함
                .filter(to -> to.getRepeatEndDate() == null || 
                        !to.getRepeatEndDate().isBefore(query.startDate()))
                .filter(to -> matchesKeyword(to, query.keyword()))
                .toList();
        
        for (TodoOriginal todoOriginal : todoOriginals) {
            List<LocalDate> virtualDates = generateVirtualDates(
                    todoOriginal, query.startDate(), query.endDate());
            
            LocalDate originalDueDate = todoOriginal.getDueDate();
            
            for (LocalDate virtualDate : virtualDates) {
                if (virtualDate.isBefore(baseDate)) {
                    continue;
                }
                
                // 원본 날짜와 같은 경우는 제외 (generateOriginalTodos에서 처리)
                if (virtualDate.equals(originalDueDate)) {
                    continue;
                }
                
                boolean alreadyCompleted = todoRepository.existsByMemberIdAndDueDateAndOriginalTodoId(
                        query.memberId(), virtualDate, todoOriginal.getId());
                
                if (!alreadyCompleted) {
                    long daysDifference = originalDueDate != null ? 
                        ChronoUnit.DAYS.between(originalDueDate, virtualDate) : 0;
                    String virtualId = todoOriginal.getId() + ":" + daysDifference;
                    
                    virtualTodos.add(createVirtualTodoResult(todoOriginal, virtualDate, virtualId));
                }
            }
        }
        
        return virtualTodos;
    }
    
    private List<TodoResult> generateOriginalTodos(TodoSearchQuery query) {
        if (query.hideStatusIds() != null && query.hideStatusIds().contains(0)) {
            return new ArrayList<>();
        }
        
        if (query.statusIds() != null && !query.statusIds().isEmpty() && 
            !query.statusIds().contains(0)) {
            return new ArrayList<>();
        }
        
        List<TodoResult> originalTodos = new ArrayList<>();
        
        List<TodoOriginal> todoOriginals = todoOriginalService.getActiveTodoOriginals(query.memberId())
                .stream()
                .filter(to -> matchesKeyword(to, query.keyword()))
                .filter(to -> matchesDateRange(to, query.startDate(), query.endDate()))
                .filter(to -> matchesCategoryFilter(to, query.categoryIds()))
                .filter(to -> matchesPriorityFilter(to, query.priorityIds()))
                .toList();
        
        for (TodoOriginal todoOriginal : todoOriginals) {
            // 이미 완료된 Todo가 있는지 확인
            boolean alreadyCompleted = todoRepository.existsByMemberIdAndDueDateAndOriginalTodoId(
                    query.memberId(), todoOriginal.getDueDate(), todoOriginal.getId());
            
            if (!alreadyCompleted) {
                String virtualId = todoOriginal.getId() + ":0";
                originalTodos.add(createOriginalTodoResult(todoOriginal, virtualId));
            }
        }
        
        return originalTodos;
    }
    
    private List<LocalDate> generateVirtualDates(TodoOriginal todoOriginal, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        
        // repeat_start_date가 null이면 반복 일정을 생성하지 않음
        LocalDate current = todoOriginal.getRepeatStartDate();
        if (current == null) {
            return dates;
        }
        
        if (todoOriginal.getRepeatType() == RepeatTypeConstants.WEEKLY && 
            todoOriginal.getDaysOfWeek() != null && !todoOriginal.getDaysOfWeek().isEmpty()) {
            return generateWeeklyVirtualDates(todoOriginal, startDate, endDate);
        }
        
        while (!current.isAfter(endDate) && 
               (todoOriginal.getRepeatEndDate() == null || !current.isAfter(todoOriginal.getRepeatEndDate()))) {
            if (!current.isBefore(startDate)) {
                dates.add(current);
            }
            current = getNextDate(current, todoOriginal.getRepeatType(), todoOriginal.getRepeatInterval());
        }
        
        return dates;
    }
    
    private List<LocalDate> generateWeeklyVirtualDates(TodoOriginal todoOriginal, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        
        // repeat_start_date가 null이면 반복 일정을 생성하지 않음
        LocalDate repeatStartDate = todoOriginal.getRepeatStartDate();
        if (repeatStartDate == null) {
            return dates;
        }
        
        LocalDate currentWeek = repeatStartDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        
        while (!currentWeek.isAfter(endDate) && 
               (todoOriginal.getRepeatEndDate() == null || !currentWeek.isAfter(todoOriginal.getRepeatEndDate()))) {
            
            for (Integer dayOfWeek : todoOriginal.getDaysOfWeek()) {
                LocalDate dateForDay = currentWeek.plusDays(dayOfWeek);
                
                if (!dateForDay.isBefore(startDate) && !dateForDay.isAfter(endDate) &&
                    !dateForDay.isBefore(repeatStartDate) &&
                    (todoOriginal.getRepeatEndDate() == null || !dateForDay.isAfter(todoOriginal.getRepeatEndDate()))) {
                    dates.add(dateForDay);
                }
            }
            
            currentWeek = currentWeek.plusWeeks(todoOriginal.getRepeatInterval());
        }
        
        dates.sort(LocalDate::compareTo);
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
    
    private TodoResult createVirtualTodoResult(TodoOriginal todoOriginal, LocalDate virtualDate, String virtualId) {
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
                0,
                "진행중",
                todoOriginal.getPriorityId(),
                priorityName,
                todoOriginal.getCategory() != null ? todoOriginal.getCategory().getId() : null,
                todoOriginal.getCategory() != null ? todoOriginal.getCategory().getName() : null,
                virtualDate,
                todoOriginal.getDueTime(),
                todoOriginal.getRepeatType(),
                todoOriginal.getRepeatInterval(),
                todoOriginal.getRepeatEndDate(),
                todoOriginal.getDaysOfWeek(),
                todoOriginal.getId(),
                todoOriginal.getTags()
        );
    }
    
    private TodoResult toTodoResult(Todo todo) {
        Integer actualStatus = todo.getActualStatus();
        String statusName = switch (actualStatus) {
            case 0 -> "진행중";
            case 1 -> "완료";
            case 2 -> "지연";
            default -> "알 수 없음";
        };
        
        String priorityName = null;
        if (todo.getPriorityId() != null) {
            priorityName = switch (todo.getPriorityId()) {
                case 0 -> "낮음";
                case 1 -> "보통";
                case 2 -> "높음";
                default -> "알 수 없음";
            };
        }
        
        return new TodoResult(
                todo.getVirtualId(),
                todo.getTitle(),
                todo.getDescription(),
                actualStatus,
                statusName,
                todo.getPriorityId(),
                priorityName,
                todo.getCategory() != null ? todo.getCategory().getId() : null,
                todo.getCategory() != null ? todo.getCategory().getName() : null,
                todo.getDueDate(),
                todo.getDueTime(),
                null,
                null,
                null,
                null,
                todo.getOriginalTodoId(),
                todo.getTags()
        );
    }
    
    private boolean matchesKeyword(TodoOriginal todoOriginal, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return (todoOriginal.getTitle() != null && todoOriginal.getTitle().toLowerCase().contains(lowerKeyword)) ||
               (todoOriginal.getDescription() != null && todoOriginal.getDescription().toLowerCase().contains(lowerKeyword)) ||
               (todoOriginal.getTags() != null && todoOriginal.getTags().stream()
                       .anyMatch(tag -> tag.toLowerCase().contains(lowerKeyword)));
    }
    
    private boolean matchesDateRange(TodoOriginal todoOriginal, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        
        LocalDate dueDate = todoOriginal.getDueDate();
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
    
    private boolean matchesCategoryFilter(TodoOriginal todoOriginal, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return true;
        }
        
        if (todoOriginal.getCategory() == null) {
            return categoryIds.contains(null);
        }
        
        return categoryIds.contains(todoOriginal.getCategory().getId());
    }
    
    private boolean matchesPriorityFilter(TodoOriginal todoOriginal, List<Integer> priorityIds) {
        if (priorityIds == null || priorityIds.isEmpty()) {
            return true;
        }
        
        return priorityIds.contains(todoOriginal.getPriorityId());
    }
    
    private TodoResult createOriginalTodoResult(TodoOriginal todoOriginal, String virtualId) {
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
                0,
                "진행중",
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
    
    private Comparator<TodoResult> getDefaultComparator() {
        return Comparator
                .comparing((TodoResult t) -> t.dueDate() == null && t.dueTime() == null && t.repeatType() == null)
                .thenComparing((TodoResult t) -> getStatusPriority(t.statusId()))
                .thenComparing((TodoResult t) -> t.dueDate() != null ? t.dueDate() : LocalDate.MAX)
                .thenComparing((TodoResult t) -> t.priorityId() != null ? -t.priorityId() : Integer.MIN_VALUE)
                .thenComparing((TodoResult t) -> Long.parseLong(t.id().split(":")[0]));
    }
    
    private int getStatusPriority(Integer statusId) {
        return switch (statusId) {
            case 2 -> 1;
            case 0 -> 2;
            case 1 -> 3;
            default -> 4;
        };
    }
}
