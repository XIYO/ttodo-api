package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.common.error.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.application.dto.result.TodoStatistics;
import point.zzicback.todo.domain.*;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
  private final TodoRepository todoRepository;
  private final CategoryRepository categoryRepository;
  private final MemberService memberService;

  public Page<TodoResult> getTodoList(TodoListQuery query) {
    updateOverdueTodos();
    
    Page<Todo> todoPage;
    
    boolean hasFilters = query.statusId() != null || query.categoryId() != null ||
                        query.priorityId() != null || (query.keyword() != null && !query.keyword().trim().isEmpty()) ||
                        query.startDate() != null || query.endDate() != null;
    
    if (hasFilters) {
      todoPage = todoRepository.findByFilters(
          query.memberId(),
          query.statusId(),
          query.categoryId(),
          query.priorityId(),
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

    return todoPage.map(this::toTodoResult);
  }

  public TodoResult getTodo(TodoQuery query) {
    updateOverdueTodos();
    
    return todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
            .map(this::toTodoResult)
            .orElseThrow(() -> new EntityNotFoundException("Todo", query.todoId()));
  }

  @Transactional
  protected void updateOverdueTodos() {
    todoRepository.updateOverdueTodos(LocalDate.now());
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
            todo.getId(),
            todo.getTitle(),
            todo.getDescription(),
            actualStatus,
            statusName,
            todo.getPriorityId(),
            priorityName,
            todo.getCategory() != null ? todo.getCategory().getId() : null,
            todo.getCategory() != null ? todo.getCategory().getName() : null,
            todo.getDueDate(),
            todo.getRepeatType(),
            todo.getTags()
    );
  }

  @Transactional
  public void createTodo(CreateTodoCommand command) {
    var member = memberService.findVerifiedMember(command.memberId());
    
    Category category = null;
    if (command.categoryId() != null) {
      category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
              .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
    }
    
    Todo todo = Todo.builder()
            .title(command.title())
            .description(command.description())
            .priorityId(command.priorityId())
            .category(category)
            .dueDate(command.dueDate())
            .repeatType(command.repeatType())
            .tags(command.tags())
            .member(member)
            .build();
    todoRepository.save(todo);
  }

  @Transactional
  public void updateTodo(UpdateTodoCommand command) {
    updateOverdueTodos();
    
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
    todo.setRepeatType(command.repeatType());
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
    if (command.repeatType() != null) {
      todo.setRepeatType(command.repeatType());
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
  
  public TodoStatistics getTodoStatistics(UUID memberId) {
    updateOverdueTodos();
    
    long total = todoRepository.countByMemberId(memberId);
    long inProgress = todoRepository.countInProgressByMemberId(memberId);
    long completed = todoRepository.countCompletedByMemberId(memberId);
    long overdue = todoRepository.countOverdueByMemberId(memberId, LocalDate.now());
    
    return new TodoStatistics(total, inProgress, completed, overdue);
  }
  
  public Page<String> getTags(UUID memberId, Pageable pageable) {
    return todoRepository.findDistinctTagsByMemberId(memberId, pageable);
  }
}