package point.zzicback.todo.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import point.zzicback.todo.application.TodoService;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.presentation.dto.*;
import point.zzicback.todo.presentation.mapper.TodoPresentationMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class TodoController {
  private final TodoService todoService;
  private final TodoPresentationMapper todoPresentationMapper;

  @GetMapping("/{memberId}/todos")
  @ResponseStatus(HttpStatus.OK)
  public Page<TodoResponse> getAll(@PathVariable UUID memberId, @RequestParam Boolean done,
                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(defaultValue = "id,desc") String sort) {
    String[] sortParams = sort.split(",");
    String sortBy = sortParams[0];
    Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    TodoListQuery query = TodoListQuery.of(memberId, done, pageable);
    return todoService.getTodoList(query).map(todoPresentationMapper::toResponse);
  }

  @GetMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.OK)
  public TodoResponse getTodo(@PathVariable UUID memberId, @PathVariable Long id) {
    return todoPresentationMapper.toResponse(todoService.getTodo(TodoQuery.of(memberId, id)));
  }

  @PostMapping("/{memberId}/todos")
  @ResponseStatus(HttpStatus.CREATED)
  public void add(@PathVariable UUID memberId, @RequestBody @Valid CreateTodoRequest request) {
    todoService.createTodo(todoPresentationMapper.toCommand(request, memberId));
  }

  @PutMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void modify(@PathVariable UUID memberId, @PathVariable Long id, @RequestBody UpdateTodoRequest request) {
    todoService.updateTodo(todoPresentationMapper.toCommand(request, memberId, id));
  }

  @DeleteMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@PathVariable UUID memberId, @PathVariable Long id) {
    todoService.deleteTodo(TodoQuery.of(memberId, id));
  }
}
