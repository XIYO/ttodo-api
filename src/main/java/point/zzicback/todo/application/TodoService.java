package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.common.error.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.*;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
  private final TodoRepository todoRepository;
  private final CategoryRepository categoryRepository;
  private final MemberService memberService;

  public Page<TodoResult> getTodoList(TodoListQuery query) {
    Page<Todo> todoPage;
    
    boolean hasFilters = query.status() != null || query.categoryId() != null || 
                        query.priority() != null || (query.keyword() != null && !query.keyword().trim().isEmpty());
    
    if (query.status() == TodoStatus.OVERDUE) {
      todoPage = todoRepository.findOverdueTodos(query.memberId(), LocalDate.now(), query.pageable());
    } else if (hasFilters) {
      todoPage = todoRepository.findByFilters(
          query.memberId(), 
          query.status(), 
          query.categoryId(), 
          query.priority(), 
          query.keyword(), 
          query.pageable()
      );
    } else {
      todoPage = todoRepository.findByMemberId(query.memberId(), query.pageable());
    }
    
    return todoPage.map(this::toTodoResult);
  }

  public TodoResult getTodo(TodoQuery query) {
    return todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
            .map(this::toTodoResult)
            .orElseThrow(() -> new EntityNotFoundException("Todo", query.todoId()));
  }

  private TodoResult toTodoResult(Todo todo) {
    TodoStatus actualStatus = todo.getActualStatus();
    return new TodoResult(
            todo.getId(),
            todo.getTitle(),
            todo.getDescription(),
            actualStatus,
            todo.getPriority(),
            todo.getCategory() != null ? todo.getCategory().getId() : null,
            todo.getCategory() != null ? todo.getCategory().getName() : null,
            todo.getDueDate(),
            todo.getRepeatType(),
            todo.getTags(),
            todo.getDisplayCategory(),
            todo.getActualDisplayStatus()
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
            .priority(command.priority())
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
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    
    Category category = null;
    if (command.categoryId() != null) {
      category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
              .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
    }
    
    todo.setTitle(command.title());
    todo.setDescription(command.description());
    todo.setStatus(command.status());
    todo.setPriority(command.priority());
    todo.setCategory(category);
    todo.setDueDate(command.dueDate());
    todo.setRepeatType(command.repeatType());
    todo.setTags(command.tags());
  }

  @Transactional
  public void partialUpdateTodo(UpdateTodoCommand command) {
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
    if (command.status() != null) {
      todo.setStatus(command.status());
    }
    if (command.priority() != null) {
      todo.setPriority(command.priority());
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
    todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
            .ifPresentOrElse(todo -> todoRepository.deleteById(query.todoId()), () -> {
              throw new EntityNotFoundException("Todo", query.todoId());
            });
  }
}