package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.experience.application.event.TodoCompletedEvent;
import point.zzicback.experience.application.event.TodoUncompletedEvent;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.DeleteTodoCommand;
import point.zzicback.todo.application.dto.command.UpdateVirtualTodoCommand;
import point.zzicback.todo.application.dto.query.TodoQuery;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import java.util.Collections;
import point.zzicback.todo.application.dto.query.VirtualTodoQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.application.dto.result.TodoStatistics;
import point.zzicback.todo.application.mapper.TodoApplicationMapper;
import point.zzicback.todo.domain.RepeatTypeConstants;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;
import point.zzicback.todo.domain.TodoOriginal;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;
import point.zzicback.todo.infrastructure.persistence.TodoSpecification;
import point.zzicback.todo.presentation.dto.response.CalendarTodoStatusResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
    
    public boolean existsVirtualTodo(UUID memberId, TodoId todoId) {
        return todoRepository.findByTodoIdAndMemberId(todoId, memberId).isPresent();
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
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndMemberIdIgnoreActive(todoId, query.memberId());
        
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
            // 원본 TodoOriginal 조회 (82:0)
            return todoOriginalService.getTodo(TodoQuery.of(query.memberId(), query.originalTodoId()));
        } else {
            // 가상 Todo 생성해서 반환
            TodoOriginal todoOriginal = todoOriginalService.getTodoOriginals(query.memberId())
                    .stream()
                    .filter(to -> to.getId().equals(query.originalTodoId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", query.originalTodoId()));
            
            LocalDate targetDate = null;
            if (todoOriginal.getRepeatStartDate() != null) {
                targetDate = todoOriginal.getRepeatStartDate().plusDays(query.daysDifference());
            } else if (todoOriginal.getDate() != null) {
                targetDate = todoOriginal.getDate().plusDays(query.daysDifference());
            } else {
                targetDate = LocalDate.now().plusDays(query.daysDifference());
            }
            
            String virtualId = query.originalTodoId() + ":" + query.daysDifference();
            return todoApplicationMapper.toVirtualResult(todoOriginal, virtualId, targetDate);
        }
    }

    @Transactional
    public void deactivateVirtualTodo(DeleteTodoCommand command) {
        TodoId todoId = new TodoId(command.originalTodoId(), command.daysDifference());
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndMemberIdIgnoreActive(todoId, command.memberId());
        
        if (existingTodo.isPresent()) {
            // 이미 Todo 테이블에 데이터가 있으면 complete=true, active=false로 설정 (삭제 표시)
            Todo todo = existingTodo.get();
            todo.setComplete(true);
            todo.setActive(false);
            todoRepository.save(todo);
        } else {
            // Todo 테이블에 데이터가 없으면 새로 생성해서 complete=true, active=true로 설정
            List<TodoOriginal> todoOriginals = todoOriginalService.getTodoOriginals(command.memberId());
            TodoOriginal todoOriginal = todoOriginals.stream()
                    .filter(to -> to.getId().equals(command.originalTodoId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("TodoOriginal", command.originalTodoId()));
            
            Member member = memberService.findByIdOrThrow(command.memberId());
            
            LocalDate targetDate = todoOriginal.getRepeatStartDate() != null ? 
                todoOriginal.getRepeatStartDate().plusDays(command.daysDifference()) :
                (todoOriginal.getDate() != null ? todoOriginal.getDate() : LocalDate.now()).plusDays(command.daysDifference());
            
            Todo newTodo = Todo.builder()
                    .todoId(todoId)
                    .title(todoOriginal.getTitle())
                    .description(todoOriginal.getDescription())
                    .complete(true)  // 삭제 표시
                    .active(false)   // 비활성화
                    .priorityId(todoOriginal.getPriorityId())
                    .category(todoOriginal.getCategory())
                    .date(targetDate)
                    .time(todoOriginal.getTime())
                    .tags(new HashSet<>(todoOriginal.getTags()))
                    .member(member)
                    .build();
            
            todoRepository.save(newTodo);
        }
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
            (todoOriginal.getDate() != null ? todoOriginal.getDate() : LocalDate.now()).plusDays(daysDifference);
        
        // active 상태에 관계없이 기존 Todo 확인
        Optional<Todo> existingTodo = todoRepository.findByTodoIdAndMemberIdIgnoreActive(todoId, command.memberId());
        
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
                    .title(command.title() != null && !command.title().trim().isEmpty() ? command.title() : todoOriginal.getTitle())
                    .description(command.description() != null && !command.description().trim().isEmpty() ? command.description() : todoOriginal.getDescription())
                    .complete(command.complete() != null ? command.complete() : false)
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

        Set<LocalDate> datesWithTodos = new HashSet<>();

        // 실제 투두(active=true, complete=false 포함) 날짜
        Specification<Todo> spec = TodoSpecification.createSpecification(
                memberId, null, null, null, startDate, endDate
        );
        Page<Todo> realTodos = todoRepository.findAll(spec, Pageable.unpaged());
        realTodos.getContent().stream()
                .filter(todo -> Boolean.TRUE.equals(todo.getActive()))
                .map(Todo::getDate)
                .filter(Objects::nonNull)
                .forEach(datesWithTodos::add);

        // 원본/반복 투두에서 해당 월의 날짜 생성
        List<TodoOriginal> originals = todoOriginalService.getTodoOriginals(memberId);
        for (TodoOriginal original : originals) {
            if (!Boolean.TRUE.equals(original.getActive())) continue;
            if (original.getDate() != null && !original.getDate().isBefore(startDate) && !original.getDate().isAfter(endDate)) {
                // 실제 투두가 없거나 삭제/비활성화가 아닌 경우만
                TodoId todoId = new TodoId(original.getId(), 0L);
                Optional<Todo> exist = todoRepository.findByTodoIdAndMemberIdIgnoreActive(todoId, memberId);
                if (exist.isEmpty() || Boolean.TRUE.equals(exist.get().getActive())) {
                    datesWithTodos.add(original.getDate());
                }
            }
            // 반복 투두
            List<LocalDate> repeatDates = generateVirtualDates(original, startDate, endDate);
            for (LocalDate date : repeatDates) {
                long daysDiff = ChronoUnit.DAYS.between(original.getRepeatStartDate(), date);
                TodoId todoId = new TodoId(original.getId(), daysDiff);
                Optional<Todo> exist = todoRepository.findByTodoIdAndMemberIdIgnoreActive(todoId, memberId);
                if (exist.isEmpty() || Boolean.TRUE.equals(exist.get().getActive())) {
                    datesWithTodos.add(date);
                }
            }
        }

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> new CalendarTodoStatusResponse(date, datesWithTodos.contains(date)))
                .toList();
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
        
        List<TodoOriginal> todoOriginals = todoOriginalService.getTodoOriginals(query.memberId())
                .stream()
                .filter(to -> to.getRepeatStartDate() != null)
                .filter(to -> to.getRepeatType() != null && to.getRepeatType() > 0)
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
                // baseDate 이후의 가상 투두만 포함
                if (virtualDate.isBefore(baseDate)) {
                    continue;
                }

                if (virtualDate.equals(originalDueDate)) {
                    continue;
                }

                Optional<Todo> existingTodo = todoRepository.findByTodoIdAndMemberIdIgnoreActive(
                        new TodoId(todoOriginal.getId(),
                                repeatStartDate != null ? ChronoUnit.DAYS.between(repeatStartDate, virtualDate) : 0),
                        query.memberId());

                boolean isDeleted = existingTodo.isPresent() && Boolean.FALSE.equals(existingTodo.get().getActive());
                if (isDeleted) {
                    continue;
                }

                if (existingTodo.isEmpty() || !Boolean.TRUE.equals(existingTodo.get().getComplete())) {
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
        LocalDate baseDate = query.date() != null ? query.date() : query.startDate();
        
        List<TodoOriginal> todoOriginals = todoOriginalService.getTodoOriginals(query.memberId())
                .stream()
                .filter(to -> matchesKeyword(to, query.keyword()))
                .filter(to -> matchesDateRange(to, query.startDate(), query.endDate()))
                .filter(to -> matchesCategoryFilter(to, query.categoryIds()))
                .filter(to -> matchesPriorityFilter(to, query.priorityIds()))
                .toList();
        
        for (TodoOriginal todoOriginal : todoOriginals) {
            // baseDate 이후의 원본 투두만 포함
            if (todoOriginal.getDate() != null && baseDate != null && todoOriginal.getDate().isBefore(baseDate)) {
                continue;
            }

            Optional<Todo> existingTodo = todoRepository.findByTodoIdAndMemberIdIgnoreActive(
                    new TodoId(todoOriginal.getId(), 0L), query.memberId());

            boolean isDeleted = existingTodo.isPresent() && Boolean.FALSE.equals(existingTodo.get().getActive());
            if (isDeleted) {
                continue;
            }

            if (existingTodo.isEmpty() || !Boolean.TRUE.equals(existingTodo.get().getComplete())) {
                if (todoOriginal.getRepeatStartDate() != null) {
                    long daysDifference = ChronoUnit.DAYS.between(
                            todoOriginal.getRepeatStartDate(), todoOriginal.getDate());
                    String virtualId = todoOriginal.getId() + ":" + daysDifference;
                    originalTodos.add(todoApplicationMapper.toOriginalResult(todoOriginal, virtualId, todoOriginal.getDate()));
                } else {
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
