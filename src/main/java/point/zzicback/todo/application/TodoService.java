package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
  private final TodoRepository todoRepository;
  private final MemberService memberService;

  public Page<TodoResult> getTodoList(TodoListQuery query) {
    Page<Todo> todoPage;
    
    // status 파라미터가 있으면 status 기준으로 필터링
    if (query.status() != null) {
      todoPage = todoRepository.findByMemberIdAndStatus(query.memberId(), query.status(), query.pageable());
    }
    // 전체 조회
    else {
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
    return new TodoResult(
            todo.getId(),
            todo.getTitle(),
            todo.getDescription(),
            todo.getStatus(),
            todo.getPriority(),
            todo.getCategory(),
            todo.getCustomCategory(),
            todo.getDueDate(),
            todo.getRepeatType(),
            todo.getTags(),
            todo.getDisplayCategory(),
            todo.getDisplayPriority(),
            todo.getDisplayStatus()
    );
  }

  @Transactional
  public void createTodo(CreateTodoCommand command) {
    validateCategoryAndCustomCategory(command.category(), command.customCategory());
    
    var member = memberService.findVerifiedMember(command.memberId());
    Todo todo = Todo.builder()
            .title(command.title())
            .description(command.description())
            .priority(command.priority())
            .category(command.category())
            .customCategory(command.customCategory())
            .dueDate(command.dueDate())
            .repeatType(command.repeatType())
            .tags(command.tags())
            .member(member)
            .build();
    todoRepository.save(todo);
  }

  @Transactional
  public void updateTodo(UpdateTodoCommand command) {
    validateCategoryAndCustomCategory(command.category(), command.customCategory());
    
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    todo.setTitle(command.title());
    todo.setDescription(command.description());
    todo.setStatus(command.status());
    todo.setPriority(command.priority());
    todo.setCategory(command.category());
    todo.setCustomCategory(command.customCategory());
    todo.setDueDate(command.dueDate());
    todo.setRepeatType(command.repeatType());
    todo.setTags(command.tags());
  }

  @Transactional
  public void partialUpdateTodo(UpdateTodoCommand command) {
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    
    // 부분 수정에서는 카테고리가 변경되는 경우에만 검증
    if (command.category() != null) {
      validateCategoryAndCustomCategory(command.category(), command.customCategory());
      todo.setCategory(command.category());
      todo.setCustomCategory(command.customCategory());
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
  
  private void validateCategoryAndCustomCategory(TodoCategory category, String customCategory) {
    if (category != null && category != TodoCategory.OTHER && customCategory != null && !customCategory.trim().isEmpty()) {
      throw new BusinessException("커스텀 카테고리는 카테고리가 '기타'일 때만 입력할 수 있습니다.");
    }
    if (category == TodoCategory.OTHER && (customCategory == null || customCategory.trim().isEmpty())) {
      throw new BusinessException("카테고리가 '기타'인 경우 커스텀 카테고리를 입력해야 합니다.");
    }
  }
}