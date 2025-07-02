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
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.*;
import point.zzicback.todo.application.mapper.TodoApplicationMapper;
import point.zzicback.todo.domain.*;
import point.zzicback.todo.infrastructure.persistence.*;
import point.zzicback.todo.presentation.dto.response.CalendarTodoStatusResponse;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VirtualTodoService {
    
    private final TodoOriginalService todoOriginalService;
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final MemberService memberService;
    private final TodoApplicationMapper todoApplicationMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    public Page<TodoResult> getTodoList(TodoSearchQuery query) {
        // 모든 실제 투두를 조회 (페이지네이션 없이)
        Page<Todo> todoPage = todoRepository.findByMemberId(
                query.memberId(),
                query.categoryIds(),
                query.complete(),
                query.priorityIds(),
                query.startDate(),
                query.endDate(),
                Pageable.unpaged());
        
        return getTodoListWithVirtualTodos(query, todoPage);
    }
    
    public TodoResult getVirtualTodo(VirtualTodoQuery query) {
        if (query.daysDifference() == 0) {
            // 원본 TodoOriginal 조회 (82:0)
            return todoOriginalService.getTodo(TodoQuery.of(query.memberId(), query.originalTodoId()));
        } else {
            // 가상 Todo 조회 (82:1, 82:2, ...)
            // 먼저 실제 완료된 Todo가 있는지 확인
            TodoId todoId = new TodoId(query.originalTodoId(), query.daysDifference());
            Optional<Todo> completedTodo = todoRepository.findByTodoIdAndMemberId(todoId, query.memberId());
            
            if (completedTodo.isPresent()) {
                // 완료된 실제 Todo 반환
                return todoApplicationMapper.toResult(completedTodo.get());
            } else {
                // 가상 Todo 생성해서 반환
                TodoOriginal todoOriginal = todoOriginalService.getTodoOriginals(query.memberId())
                        .stream()
                        .filter(to -> to.getId().equals(query.originalTodoId()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", query.originalTodoId()));
                
                LocalDate targetDate = todoOriginal.getRepeatStartDate() != null ? 
                    todoOriginal.getRepeatStartDate().plusDays(query.daysDifference()) :
                    todoOriginal.getDate().plusDays(query.daysDifference());
                
                String virtualId = query.originalTodoId() + ":" + query.daysDifference();
                return todoApplicationMapper.toVirtualResult(todoOriginal, virtualId, targetDate);
            }
        }
    }
    
    @Transactional
    public void deleteRepeatTodo(DeleteRepeatTodoCommand command) {
        List<TodoOriginal> todoOriginals = todoOriginalService.getTodoOriginals(command.memberId());
        TodoOriginal todoOriginal = todoOriginals.stream()
                .filter(to -> to.getId().equals(command.originalTodoId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.originalTodoId()));
        
        LocalDate stopDate = todoOriginal.getRepeatStartDate() != null ? 
            todoOriginal.getRepeatStartDate().plusDays(command.daysDifference()) :
            todoOriginal.getDate().plusDays(command.daysDifference());
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
        
        List<TodoOriginal> todoOriginals = todoOriginalService.getTodoOriginals(command.memberId());
        TodoOriginal todoOriginal = todoOriginals.stream()
                .filter(to -> to.getId().equals(originalTodoId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", originalTodoId));
        
        LocalDate targetDate = todoOriginal.getRepeatStartDate() != null ? 
            todoOriginal.getRepeatStartDate().plusDays(daysDifference) :
            todoOriginal.getDate().plusDays(daysDifference);
        
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndMemberId(todoId, command.memberId());
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        if (existingTodo.isPresent()) {
            Todo todo = existingTodo.get();
            boolean wasIncomplete = !Boolean.TRUE.equals(todo.getComplete());
            
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
                Category category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
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
            
            return todoApplicationMapper.toResult(todo);
        } else {
            // 새 Todo 생성
            Todo newTodo = Todo.builder()
                    .todoId(todoId)
                    .title(command.title() != null && !command.title().trim().isEmpty() ? command.title() : todoOriginal.getTitle())
                    .description(command.description() != null && !command.description().trim().isEmpty() ? command.description() : todoOriginal.getDescription())
                    .complete(command.complete() != null ? command.complete() : true) // 기본값은 완료
                    .priorityId(command.priorityId() != null ? command.priorityId() : todoOriginal.getPriorityId())
                    .category(todoOriginal.getCategory())
                    .date(command.date() != null ? command.date() : targetDate)
                    .time(command.time() != null ? command.time() : todoOriginal.getTime())
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
    
    public List<CalendarTodoStatusResponse> getMonthlyTodoStatus(UUID memberId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        TodoSearchQuery query = new TodoSearchQuery(
                memberId,
                null, null, null, null, null, null,
                startDate, endDate,
                PageRequest.of(0, 1000)
        );
        
        Set<LocalDate> datesWithTodos = getTodoList(query).getContent().stream()
                .map(TodoResult::date)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> new CalendarTodoStatusResponse(date, datesWithTodos.contains(date)))
                .toList();
    }
    
    public TodoStatistics getTodoStatistics(UUID memberId, LocalDate targetDate) {
        // 실제 Todo (DB에 저장된) 조회
        Page<Todo> realTodoPage = todoRepository.findByMemberId(
                memberId,
                null, // categoryIds
                null, // complete - 모든 상태 조회
                null, // priorityIds
                targetDate, // startDate = 대상 날짜
                targetDate, // endDate = 대상 날짜
                Pageable.unpaged()
        );
        
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
        // 실제 투두는 이미 DB에서 필터링되었으므로 추가 필터링 불필요
        List<TodoResult> realTodos = todoPage.getContent().stream()
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
        
        List<TodoOriginal> todoOriginals = todoOriginalService.getTodoOriginals(query.memberId())
                .stream()
                .filter(to -> to.getRepeatStartDate() != null) // repeat_start_date가 null이면 반복 사용 안함
                .filter(to -> to.getRepeatEndDate() == null || 
                        !to.getRepeatEndDate().isBefore(query.startDate()))
                .filter(to -> matchesKeyword(to, query.keyword()))
                .toList();
        
        for (TodoOriginal todoOriginal : todoOriginals) {
            List<LocalDate> virtualDates = generateVirtualDates(
                    todoOriginal, query.startDate(), query.endDate());
            
            LocalDate originalDueDate = todoOriginal.getDate();
            LocalDate repeatStartDate = todoOriginal.getRepeatStartDate();
            
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
                    // repeat_start_date 기준으로 daysDifference 계산
                    long daysDifference = repeatStartDate != null ? 
                        ChronoUnit.DAYS.between(repeatStartDate, virtualDate) : 0;
                    String virtualId = todoOriginal.getId() + ":" + daysDifference;
                    
                    virtualTodos.add(todoApplicationMapper.toVirtualResult(todoOriginal, virtualId, virtualDate));
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
        
        List<TodoOriginal> todoOriginals = todoOriginalService.getTodoOriginals(query.memberId())
                .stream()
                .filter(to -> matchesKeyword(to, query.keyword()))
                .filter(to -> matchesDateRange(to, query.startDate(), query.endDate()))
                .filter(to -> matchesCategoryFilter(to, query.categoryIds()))
                .filter(to -> matchesPriorityFilter(to, query.priorityIds()))
                .toList();
        
        for (TodoOriginal todoOriginal : todoOriginals) {
            // 이미 완료된 Todo가 있는지 확인
            boolean alreadyCompleted = todoRepository.existsByMemberIdAndDueDateAndOriginalTodoId(
                    query.memberId(), todoOriginal.getDate(), todoOriginal.getId());
            
            if (!alreadyCompleted) {
                if (todoOriginal.getRepeatStartDate() != null) {
                    // 반복 투두: repeat_start_date 기준으로 daysDifference 계산
                    long daysDifference = ChronoUnit.DAYS.between(
                        todoOriginal.getRepeatStartDate(), todoOriginal.getDate());
                    String virtualId = todoOriginal.getId() + ":" + daysDifference;
                    originalTodos.add(todoApplicationMapper.toOriginalResult(todoOriginal, virtualId, todoOriginal.getDate()));
                } else {
                    // 일반 투두: 항상 :0
                    String virtualId = todoOriginal.getId() + ":0";
                    originalTodos.add(todoApplicationMapper.toOriginalResult(todoOriginal, virtualId, todoOriginal.getDate()));
                }
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
                
                // due_date와 중복되지 않고, 조회 범위 안에 있으며, repeat_start_date 이후인 경우만 포함
                if (!dateForDay.equals(todoOriginal.getDate()) && // 🆕 due_date와 다르고
                    !dateForDay.isBefore(startDate) && !dateForDay.isAfter(endDate) &&
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
        
        LocalDate dueDate = todoOriginal.getDate();
        if (dueDate == null) {
            return true; // null인 경우는 항상 포함
        }
        
        if (startDate != null && dueDate.isBefore(startDate)) {
            return false;
        }

        return endDate == null || !dueDate.isAfter(endDate);
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
    
    private Comparator<TodoResult> getDefaultComparator() {
        return Comparator
                .comparing((TodoResult t) -> t.date() == null && t.time() == null && t.repeatType() == null)
                .thenComparing((TodoResult t) -> t.complete() != null ? t.complete() : false)
                .thenComparing((TodoResult t) -> t.date() != null ? t.date() : LocalDate.MAX)
                .thenComparing((TodoResult t) -> t.priorityId() != null ? -t.priorityId() : Integer.MIN_VALUE)
                .thenComparing((TodoResult t) -> Long.parseLong(t.id().split(":")[0]));
    }
}
