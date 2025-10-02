package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.experience.application.event.*;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.error.EntityNotFoundException;
import point.ttodoApi.todo.application.command.*;
import point.ttodoApi.todo.application.mapper.TodoApplicationMapper;
import point.ttodoApi.todo.application.query.*;
import point.ttodoApi.todo.application.result.*;
import point.ttodoApi.todo.domain.*;
import point.ttodoApi.todo.domain.recurrence.*;
import point.ttodoApi.todo.infrastructure.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoInstanceService {

  private final TodoTemplateService todoTemplateService;
  private final TodoRepository todoRepository;
  private final CategoryRepository categoryRepository;
  private final UserService UserService;
  private final TodoApplicationMapper todoApplicationMapper;
  private final ApplicationEventPublisher eventPublisher;

  public boolean existsVirtualTodo(UUID userId, TodoId todoId) {
    return todoRepository.findByTodoIdAndOwnerId(todoId, userId).isPresent();
  }

  public Page<TodoResult> getTodoList(TodoSearchQuery query) {
    Specification<Todo> spec = TodoSpecification.createSpecification(
            query.userId(),
            query.complete(),
            query.categoryIds(),
            query.priorityIds(),
            query.startDate(),
            query.endDate()
    );

    // DB 페이지네이션 복원: Pageable 객체 사용
    Page<Todo> todoPage = todoRepository.findAll(spec, query.pageable());
    return getTodoListWithVirtualTodos(query, todoPage);
  }

  public TodoResult getVirtualTodo(VirtualTodoQuery query) {
    TodoId todoId = new TodoId(query.originalTodoId(), query.daysDifference());

    // 먼저 Todo 테이블에서 확인 (active 상태 무관)
    Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(todoId, query.userId());

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
      return todoTemplateService.getTodo(TodoQuery.of(query.userId(), query.originalTodoId()));
    } else {
      // 가상 Todo 생성해서 반환
      TodoTemplate todoTemplate = todoTemplateService.getTodoTemplates(query.userId())
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
    Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(todoId, command.userId());

    if (existingTodo.isPresent()) {
      // 이미 Todo 테이블에 데이터가 있으면 complete=true, active=false로 설정 (삭제 표시)
      Todo todo = existingTodo.get();
      todo.setComplete(true);
      todo.setActive(false);
      todoRepository.save(todo);
    } else {
      // Todo 테이블에 데이터가 없으면 새로 생성해서 complete=true, active=true로 설정
      List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(command.userId());
      TodoTemplate todoTemplate = todoTemplates.stream()
              .filter(to -> to.getId().equals(command.originalTodoId()))
              .findFirst()
              .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", command.originalTodoId()));

      User user = UserService.findByIdOrThrow(command.userId());

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
              .owner(user)
              .build();

      todoRepository.save(newTodo);
    }
  }

  @Transactional
  public TodoResult updateOrCreateVirtualTodo(UpdateVirtualTodoCommand command) {
    TodoId todoId = TodoId.fromVirtualId(command.virtualTodoId());
    Long originalTodoId = todoId.getId();
    Long daysDifference = todoId.getSeq();

    List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(command.userId());
    TodoTemplate todoTemplate = todoTemplates.stream()
            .filter(to -> to.getId().equals(originalTodoId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("TodoTemplate", originalTodoId));

    LocalDate anchor = todoTemplate.getAnchorDate() != null ? todoTemplate.getAnchorDate() :
            (todoTemplate.getDate() != null ? todoTemplate.getDate() : LocalDate.now());
    LocalDate targetDate = anchor.plusDays(daysDifference);

    // active 상태에 관계없이 기존 Todo 확인
    Optional<Todo> existingTodo = todoRepository.findByTodoIdAndOwnerIdIgnoreActive(todoId, command.userId());

    User user = UserService.findByIdOrThrow(command.userId());

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
        Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.userId())
                .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
        todo.setCategory(category);
      }

      todoRepository.save(todo);

      // 투두 완료 시 경험치 이벤트 발생
      if (wasIncomplete && Boolean.TRUE.equals(todo.getComplete())) {
        eventPublisher.publishEvent(new TodoCompletedEvent(
                command.userId(),
                originalTodoId,
                todo.getTitle()
        ));
      }

      // 투두 완료 취소 시 경험치 차감 이벤트 발생
      if (wasComplete && Boolean.FALSE.equals(todo.getComplete())) {
        eventPublisher.publishEvent(new TodoUncompletedEvent(
                command.userId(),
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
              .owner(user)
              .build();

      // 카테고리 변경이 있는 경우
      if (command.categoryId() != null) {
        Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.userId())
                .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
        newTodo.setCategory(category);
      }

      todoRepository.save(newTodo);

      // 새로 생성된 투두가 완료 상태인 경우 경험치 이벤트 발생
      if (Boolean.TRUE.equals(newTodo.getComplete())) {
        eventPublisher.publishEvent(new TodoCompletedEvent(
                command.userId(),
                originalTodoId,
                newTodo.getTitle()
        ));
      }

      return todoApplicationMapper.toResult(newTodo);
    }
  }

  public TodoStatistics getTodoStatistics(UUID userId, LocalDate targetDate) {
    // 실제 Todo (DB에 저장된) 조회
    Specification<Todo> spec = TodoSpecification.createSpecification(
            userId, null, null, null, targetDate, targetDate
    );
    Page<Todo> realTodoPage = todoRepository.findAll(spec, Pageable.unpaged());

    // TodoSearchQuery로 가상 투두 포함 전체 조회
    TodoSearchQuery query = new TodoSearchQuery(
            userId,
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
            .filter(todo -> Boolean.TRUE.equals(todo.getActive()))
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

    // IndexOutOfBoundsException 방지: offset이 size를 초과하면 빈 리스트 반환
    int start = Math.min((int) query.pageable().getOffset(), allTodos.size());
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

    List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(query.userId())
            .stream()
            .filter(to -> to.getRecurrenceRule() != null && to.getAnchorDate() != null)
            .filter(to -> matchesKeyword(to, query.keyword()))
            .toList();

    // N+1 방지: 먼저 필요한 모든 TodoId를 수집
    List<TodoId> allTodoIds = new ArrayList<>();
    for (TodoTemplate todoTemplate : todoTemplates) {
      RecurrenceRule rule = todoTemplate.getRecurrenceRule();
      List<LocalDate> virtualDates = RecurrenceEngine.generateBetween(rule, query.startDate(), query.endDate());
      LocalDate anchor = todoTemplate.getAnchorDate() != null ? todoTemplate.getAnchorDate() : todoTemplate.getDate();
      LocalDate originalDueDate = todoTemplate.getDate();

      for (LocalDate virtualDate : virtualDates) {
        if (virtualDate.isBefore(baseDate) || virtualDate.equals(originalDueDate)) {
          continue;
        }
        long diff = anchor != null ? ChronoUnit.DAYS.between(anchor, virtualDate) : 0;
        allTodoIds.add(new TodoId(todoTemplate.getId(), diff));
      }
    }

    // 한 번에 벌크 조회 후 Map으로 캐싱
    Map<TodoId, Todo> existingTodoMap = todoRepository
            .findAllByTodoIdInAndOwnerIdIgnoreActive(allTodoIds, query.userId())
            .stream()
            .collect(Collectors.toMap(Todo::getTodoId, todo -> todo));

    // Map에서 조회하여 가상 투두 생성
    for (TodoTemplate todoTemplate : todoTemplates) {
      RecurrenceRule rule = todoTemplate.getRecurrenceRule();
      List<LocalDate> virtualDates = RecurrenceEngine.generateBetween(rule, query.startDate(), query.endDate());
      LocalDate originalDueDate = todoTemplate.getDate();
      LocalDate anchor = todoTemplate.getAnchorDate() != null ? todoTemplate.getAnchorDate() : todoTemplate.getDate();

      for (LocalDate virtualDate : virtualDates) {
        if (virtualDate.isBefore(baseDate) || virtualDate.equals(originalDueDate)) {
          continue;
        }

        long diff = anchor != null ? ChronoUnit.DAYS.between(anchor, virtualDate) : 0;
        TodoId todoId = new TodoId(todoTemplate.getId(), diff);
        Todo existingTodo = existingTodoMap.get(todoId);

        // 삭제된 투두는 제외
        boolean isDeleted = existingTodo != null && Boolean.FALSE.equals(existingTodo.getActive());
        if (isDeleted) {
          continue;
        }

        // 존재하지 않거나 미완료인 경우만 가상 투두로 표시
        if (existingTodo == null || !Boolean.TRUE.equals(existingTodo.getComplete())) {
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

    List<TodoTemplate> todoTemplates = todoTemplateService.getTodoTemplates(query.userId())
            .stream()
            .filter(to -> matchesKeyword(to, query.keyword()))
            .filter(to -> matchesDateRange(to, query.startDate(), query.endDate()))
            .filter(to -> matchesCategoryFilter(to, query.categoryIds()))
            .filter(to -> matchesPriorityFilter(to, query.priorityIds()))
            .toList();

    // N+1 방지: 모든 원본 TodoId(seq=0) 수집
    List<TodoId> originalTodoIds = todoTemplates.stream()
            .map(template -> new TodoId(template.getId(), 0L))
            .toList();

    // 한 번에 벌크 조회 후 Map으로 캐싱
    Map<TodoId, Todo> existingTodoMap = todoRepository
            .findAllByTodoIdInAndOwnerIdIgnoreActive(originalTodoIds, query.userId())
            .stream()
            .collect(Collectors.toMap(Todo::getTodoId, todo -> todo));

    for (TodoTemplate todoTemplate : todoTemplates) {
      // baseDate 이후의 원본 투두만 포함
      if (todoTemplate.getDate() != null && baseDate != null && todoTemplate.getDate().isBefore(baseDate)) {
        continue;
      }

      TodoId originalTodoId = new TodoId(todoTemplate.getId(), 0L);
      Todo existingTodo = existingTodoMap.get(originalTodoId);

      // 삭제된 투두는 제외
      boolean isDeleted = existingTodo != null && Boolean.FALSE.equals(existingTodo.getActive());
      if (isDeleted) {
        continue;
      }

      // 존재하지 않거나 미완료인 경우만 원본 투두로 표시
      if (existingTodo == null || !Boolean.TRUE.equals(existingTodo.getComplete())) {
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

  private boolean matchesCategoryFilter(TodoTemplate todoTemplate, List<UUID> categoryIds) {
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
