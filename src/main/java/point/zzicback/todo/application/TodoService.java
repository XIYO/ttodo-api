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
public class TodoService {
  private final TodoRepository todoRepository;
  private final CategoryRepository categoryRepository;
  private final MemberService memberService;
  private final RepeatTodoService repeatTodoService;

  public Page<TodoResult> getTodoList(TodoSearchQuery query) {
    updateOverdueTodos();
    
    Page<Todo> todoPage;
    
    boolean hasFilters = 
        (query.statusIds() != null && !query.statusIds().isEmpty()) ||
        (query.categoryIds() != null && !query.categoryIds().isEmpty()) ||
        (query.priorityIds() != null && !query.priorityIds().isEmpty()) ||
        (query.tags() != null && !query.tags().isEmpty()) ||
        (query.keyword() != null && !query.keyword().trim().isEmpty());
    
    if (hasFilters) {
      todoPage = todoRepository.findByFilters(
          query.memberId(),
          query.statusIds(),
          query.categoryIds(),
          query.priorityIds(),
          query.tags(),
          query.keyword(),
          query.hideStatusIds(),
          query.startDate(),
          query.endDate(),
          query.pageable()
      );
    } else {
      todoPage = todoRepository.findByMemberId(
          query.memberId(),
          query.hideStatusIds(),
          query.startDate(),
          query.endDate(),
          query.pageable());
    }

    return getTodoListWithVirtualTodos(query, todoPage);
  }

  public TodoResult getTodo(TodoQuery query) {
    updateOverdueTodos();
    
    return todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
            .map(this::toTodoResult)
            .orElseThrow(() -> new EntityNotFoundException("Todo", query.todoId()));
  }

  @Transactional
  protected void updateOverdueTodos() {
    LocalDate today = LocalDate.now();
    LocalTime nowTime = LocalTime.now();
    todoRepository.updateOverdueTodos(today, nowTime);
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
    
    // 반복 정보 가져오기 (원본 투두이거나 originalTodoId가 있는 경우)
    Long targetTodoId = todo.getOriginalTodoId() != null ? todo.getOriginalTodoId() : todo.getId();
    RepeatTodo repeatTodo = null;
    if (targetTodoId != null) {
      repeatTodo = repeatTodoService.getRepeatTodoByTodoId(targetTodoId);
    }
    
    return new TodoResult(
            String.valueOf(todo.getId()),
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
            repeatTodo != null ? repeatTodo.getRepeatType() : null,
            repeatTodo != null ? repeatTodo.getRepeatInterval() : null,
            repeatTodo != null ? repeatTodo.getRepeatEndDate() : null,
            repeatTodo != null ? repeatTodo.getDaysOfWeek() : null,
            todo.getOriginalTodoId(),
            todo.getTags()
    );
  }

  @Transactional
  public void createTodo(CreateTodoCommand command) {
    updateOverdueTodos();
    
    Member member = memberService.findByIdOrThrow(command.memberId());
    Category category = null;
    if (command.categoryId() != null) {
      category = categoryRepository.findById(command.categoryId())
              .orElseThrow(() -> new EntityNotFoundException("Category", command.categoryId()));
    }
    
    Set<String> tags = command.tags() != null ? command.tags() : new HashSet<>();
    
    Todo todo = Todo.builder()
            .title(command.title())
            .description(command.description())
            .priorityId(command.priorityId())
            .category(category)
            .dueDate(command.dueDate())
            .dueTime(command.dueTime())
            .tags(tags)
            .member(member)
            .build();
    
    todoRepository.save(todo);
    
    if (command.repeatType() != null && !command.repeatType().equals(RepeatTypeConstants.NONE) 
        && command.repeatStartDate() != null) {
      repeatTodoService.createRepeatTodo(
              todo,
              command.repeatType(),
              command.repeatInterval(),
              command.repeatStartDate(),
              command.repeatEndDate(),
              member,
              command.daysOfWeek()
      );
    }
  }

  @Transactional
  public void updateTodo(UpdateTodoCommand command) {
    updateOverdueTodos();
    
    if (command.todoId() == null) {
      throw new BusinessException("가상 투두는 POST /api/todos/virtual/{originalTodoId}/complete API를 사용하세요");
    }
    
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    
    Category category = null;
    if (command.categoryId() != null) {
      category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
              .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
    }
    
    todo.setTitle(command.title());
    todo.setDescription(command.description());
    todo.setStatusId(command.statusId());
    todo.setPriorityId(command.priorityId());
    todo.setCategory(category);
    todo.setDueDate(command.dueDate());
    todo.setDueTime(command.dueTime());
    todo.setTags(command.tags());
  }

  @Transactional
  public void partialUpdateTodo(UpdateTodoCommand command) {
    updateOverdueTodos();
    
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    
    if (command.categoryId() != null) {
      Category category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
              .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
      todo.setCategory(category);
    }
    
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
    if (command.tags() != null) {
      todo.setTags(command.tags());
    }
  }

  @Transactional
  public void deleteTodo(TodoQuery query) {
    updateOverdueTodos();
    
    todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
            .ifPresentOrElse(todo -> todoRepository.deleteById(query.todoId()), () -> {
              throw new EntityNotFoundException("Todo", query.todoId());
            });
  }

  @Transactional
  public TodoResult completeVirtualTodo(CompleteVirtualTodoCommand command) {
    repeatTodoService.completeRepeatTodo(
        command.memberId(), 
        command.originalTodoId(), 
        command.completionDate()
    );
    
    return todoRepository.findByMemberIdAndDueDateAndOriginalTodoId(
        command.memberId(), command.completionDate(), command.originalTodoId())
        .map(this::toTodoResult)
        .orElseThrow(() -> new EntityNotFoundException("Todo", command.originalTodoId()));
  }

  public TodoStatistics getTodoStatistics(UUID memberId) {
    updateOverdueTodos();
    
    long total = todoRepository.countByMemberId(memberId);
    long inProgress = todoRepository.countInProgressByMemberId(memberId);
    long completed = todoRepository.countCompletedByMemberId(memberId);
    long overdue = todoRepository.countOverdueByMemberId(memberId, LocalDate.now(), LocalTime.now());
    
    return new TodoStatistics(total, inProgress, completed, overdue);
  }
  
  public Page<String> getTags(UUID memberId, List<Long> categoryIds, Pageable pageable) {
    return todoRepository.findDistinctTagsByMemberId(memberId, categoryIds, pageable);
  }
  
  private Page<TodoResult> getTodoListWithVirtualTodos(TodoSearchQuery query, Page<Todo> todoPage) {
    List<TodoResult> realTodos = todoPage.getContent().stream()
            .map(this::toTodoResult)
            .toList();
    
    List<TodoResult> virtualTodos = generateVirtualTodos(query);
    
    // 가상 투두가 없으면 실제 투두만 정렬하여 반환
    if (virtualTodos.isEmpty()) {
      // 사용자가 명시적으로 정렬을 지정하지 않았을 때만 기본 정렬 적용
      if (query.pageable().getSort().isUnsorted()) {
        realTodos = realTodos.stream()
            .sorted(Comparator
                .comparing((TodoResult t) -> t.dueDate() != null ? t.dueDate() : LocalDate.MAX)
                .thenComparing((TodoResult t) -> getStatusPriority(t.statusId()))
                .thenComparing((TodoResult t) -> t.priorityId() != null ? -t.priorityId() : Integer.MIN_VALUE)
                .thenComparing((TodoResult t) -> Long.parseLong(t.id().split(":")[0])))
            .toList();
      }
      return new PageImpl<>(realTodos, query.pageable(), todoPage.getTotalElements());
    }
    
    // 가상 투두가 있는 경우에만 합치고 재정렬/페이징
    List<TodoResult> allTodos = new ArrayList<>();
    allTodos.addAll(realTodos);
    allTodos.addAll(virtualTodos);
    
    // 사용자가 명시적으로 정렬을 지정하지 않았을 때만 기본 정렬 적용
    if (query.pageable().getSort().isUnsorted()) {    // 날짜별, 상태별(지연->진행중->완료), 우선순위별(높음->보통->낮음), ID순 정렬
    allTodos.sort(Comparator
        .comparing((TodoResult t) -> t.dueDate() != null ? t.dueDate() : LocalDate.MAX)
        .thenComparing((TodoResult t) -> getStatusPriority(t.statusId()))
        .thenComparing((TodoResult t) -> t.priorityId() != null ? -t.priorityId() : Integer.MIN_VALUE)
        .thenComparing((TodoResult t) -> Long.parseLong(t.id().split(":")[0])));
    }
    
    Pageable pageable = query.pageable();
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allTodos.size());
    
    List<TodoResult> pageContent = allTodos.subList(start, end);
    
    return new PageImpl<>(pageContent, pageable, allTodos.size());
  }
  
  private List<TodoResult> generateVirtualTodos(TodoSearchQuery query) {
    if (query.startDate() == null || query.endDate() == null) {
        return new ArrayList<>();
    }
    
    if (query.hideStatusIds() != null && query.hideStatusIds().contains(0)) {
        return new ArrayList<>();
    }
    
    if (query.statusIds() != null && !query.statusIds().isEmpty() && !query.statusIds().contains(0)) {
        return new ArrayList<>();
    }
    
    List<TodoResult> virtualTodos = new ArrayList<>();
    
    LocalDate baseDate = query.date() != null ? query.date() : query.startDate();
    
    List<RepeatTodo> repeatTodos = repeatTodoService.getActiveRepeatTodos(query.memberId())
            .stream()
            .filter(repeatTodo -> repeatTodo.getRepeatEndDate() == null || 
                    !repeatTodo.getRepeatEndDate().isBefore(query.startDate()))
            .filter(repeatTodo -> matchesKeyword(repeatTodo.getTodo(), query.keyword()))
            .toList();
    
    for (RepeatTodo repeatTodo : repeatTodos) {
      List<LocalDate> virtualDates = repeatTodoService.generateVirtualDates(
              repeatTodo, query.startDate(), query.endDate());
      
      LocalDate originalDueDate = repeatTodo.getTodo().getDueDate();
      
      int repeatCount = 1;
      
      for (LocalDate virtualDate : virtualDates) {
        if (virtualDate.isBefore(baseDate)) {
          continue;
        }
        
        if (virtualDate.equals(originalDueDate)) {
          continue;
        }
        
        boolean alreadyCompleted = todoRepository.existsByMemberIdAndDueDateAndOriginalTodoId(
                query.memberId(), virtualDate, repeatTodo.getTodo().getId());
        
        if (!alreadyCompleted) {
          String virtualId = repeatTodo.getTodo().getId() + ":" + repeatCount;
          virtualTodos.add(createVirtualTodoResult(repeatTodo, virtualDate, virtualId));
          repeatCount++;
        }
      }
    }
    
    return virtualTodos;
  }
  
  private TodoResult createVirtualTodoResult(RepeatTodo repeatTodo, LocalDate virtualDate, String virtualId) {
    Todo originalTodo = repeatTodo.getTodo();
    String priorityName = null;
    if (originalTodo.getPriorityId() != null) {
      priorityName = switch (originalTodo.getPriorityId()) {
        case 0 -> "낮음";
        case 1 -> "보통";
        case 2 -> "높음";
        default -> "알 수 없음";
      };
    }
    
    return new TodoResult(
            virtualId,  // 가상 ID 사용
            originalTodo.getTitle(),
            originalTodo.getDescription(),
            0,
            "진행중",
            originalTodo.getPriorityId(),
            priorityName,
            originalTodo.getCategory() != null ? originalTodo.getCategory().getId() : null,
            originalTodo.getCategory() != null ? originalTodo.getCategory().getName() : null,
            virtualDate,
            originalTodo.getDueTime(),
            repeatTodo.getRepeatType(),
            repeatTodo.getRepeatInterval(),
            repeatTodo.getRepeatEndDate(),
            repeatTodo.getDaysOfWeek(),
            originalTodo.getId(),
            originalTodo.getTags()
    );
  }
  
  private boolean matchesKeyword(Todo todo, String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return true;
    }
    
    String lowerKeyword = keyword.toLowerCase();
    
    // 제목에서 검색
    if (todo.getTitle() != null && todo.getTitle().toLowerCase().contains(lowerKeyword)) {
      return true;
    }
    
    // 설명에서 검색
    if (todo.getDescription() != null && todo.getDescription().toLowerCase().contains(lowerKeyword)) {
      return true;
    }
    
    // 태그에서 검색
    if (todo.getTags() != null) {
      for (String tag : todo.getTags()) {
        if (tag.toLowerCase().contains(lowerKeyword)) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  private int getStatusPriority(Integer statusId) {
    return switch (statusId) {
      case 2 -> 1; // 지연
      case 0 -> 2; // 진행중  
      case 1 -> 3; // 완료
      default -> 4; // 알 수 없음
    };
  }

}