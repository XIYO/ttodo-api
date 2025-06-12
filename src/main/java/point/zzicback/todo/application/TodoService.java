package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.command.UpdateTodoCommand;
import point.zzicback.todo.application.dto.query.TodoListQuery;
import point.zzicback.todo.application.dto.query.TodoQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
  private final TodoRepository todoRepository;
  private final MemberService memberService;

  public Page<TodoResult> getTodoList(TodoListQuery query) {
    Page<Todo> todoPage = todoRepository.findByMemberIdAndDone(query.memberId(), query.done(), query.pageable());
    return todoPage.map(todo -> new TodoResult(todo.getId(), todo.getTitle(), todo.getDescription(), todo.getDone()));
  }

  public TodoResult getTodo(TodoQuery query) {
    return todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
            .map(todo -> new TodoResult(todo.getId(), todo.getTitle(), todo.getDescription(), todo.getDone()))
            .orElseThrow(() -> new EntityNotFoundException("Todo", query.todoId()));
  }

  @Transactional
  public void createTodo(CreateTodoCommand command) {
    var member = memberService.findVerifiedMember(command.memberId());
    Todo todo = Todo.builder()
            .title(command.title())
            .description(command.description())
            .done(false)
            .build();
    todo.setMember(member);
    todoRepository.save(todo);
  }

  @Transactional
  public void updateTodo(UpdateTodoCommand command) {
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    todo.setTitle(command.title());
    todo.setDescription(command.description());
    todo.setDone(command.done());
  }

  @Transactional
  public void partialUpdateTodo(UpdateTodoCommand command) {
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    if (command.title() != null) {
      todo.setTitle(command.title());
    }
    if (command.description() != null) {
      todo.setDescription(command.description());
    }
    if (command.done() != null) {
      todo.setDone(command.done());
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