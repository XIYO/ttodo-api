package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import point.zzicback.todo.application.TodoService;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.domain.*;
import point.zzicback.todo.presentation.dto.*;
import point.zzicback.todo.presentation.mapper.TodoPresentationMapper;

import java.util.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class TodoController {
  private final TodoService todoService;
  private final TodoPresentationMapper todoPresentationMapper;

  @GetMapping("/{memberId}/todos")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 목록 조회", description = "사용자의 Todo 목록을 조회합니다. status 파라미터로 상태별 필터링이 가능합니다.")
  public Page<TodoResponse> getAll(
          @PathVariable UUID memberId,
          
          @RequestParam(required = false)
          @Parameter(description = "할일 상태 필터", 
                     schema = @Schema(allowableValues = {"IN_PROGRESS", "COMPLETED", "OVERDUE"}),
                     example = "IN_PROGRESS")
          TodoStatus status,
          
          @RequestParam(defaultValue = "0") int page, 
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id,desc") String sort) {
    
    String[] sortParams = sort.split(",");
    String sortBy = sortParams[0];
    Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    
    TodoListQuery query = status != null 
        ? TodoListQuery.of(memberId, status, pageable)
        : TodoListQuery.ofAll(memberId, pageable);
    
    return todoService.getTodoList(query).map(todoPresentationMapper::toResponse);
  }

  @GetMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 상세 조회", description = "특정 Todo의 상세 정보를 조회합니다.")
  public TodoResponse getTodo(@PathVariable UUID memberId, @PathVariable Long id) {
    return todoPresentationMapper.toResponse(todoService.getTodo(TodoQuery.of(memberId, id)));
  }

  @PostMapping(value = "/{memberId}/todos", consumes = {"application/json", "application/x-www-form-urlencoded"})
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Todo 생성", 
      description = "새로운 Todo를 생성합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 생성 정보",
          required = true,
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = CreateTodoRequest.class),
                  examples = @ExampleObject(
                      name = "JSON 예시",
                      value = """
                          {
                            "title": "영어 공부하기",
                            "description": "토익 문제집 2장 풀어보기",
                            "status": "IN_PROGRESS",
                            "priority": 1,
                            "categoryId": 1,
                            "dueDate": "2026-01-01",
                            "repeatType": "NONE",
                            "tags": ["영어", "학습"]
                          }
                          """
                  )
              ),
              @Content(
                  mediaType = "application/x-www-form-urlencoded",
                  schema = @Schema(implementation = CreateTodoRequest.class)
              )
          }
      )
  )
  public void add(@PathVariable UUID memberId, 
                 @Valid @RequestBody CreateTodoRequest request) {
    todoService.createTodo(todoPresentationMapper.toCommand(request, memberId));
  }

  @PutMapping(value = "/{memberId}/todos/{id}", consumes = {"application/json", "application/x-www-form-urlencoded"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 수정", 
      description = "Todo를 전체 수정합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 수정 정보",
          required = true,
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = UpdateTodoRequest.class),
                  examples = @ExampleObject(
                      name = "JSON 예시",
                      value = """
                          {
                            "title": "영어 공부하기 (수정)",
                            "description": "토익 문제집 3장 풀어보기",
                            "status": "COMPLETED",
                            "priority": 2,
                            "categoryId": 1,
                            "dueDate": "2026-01-02",
                            "repeatType": "DAILY",
                            "tags": ["영어", "학습", "토익"]
                          }
                          """
                  )
              ),
              @Content(
                  mediaType = "application/x-www-form-urlencoded",
                  schema = @Schema(implementation = UpdateTodoRequest.class)
              )
          }
      )
  )
  public void modify(@PathVariable UUID memberId, 
                    @PathVariable Long id,
                    @Valid @RequestBody UpdateTodoRequest request) {
    todoService.updateTodo(todoPresentationMapper.toCommand(request, memberId, id));
  }

  @PatchMapping(value = "/{memberId}/todos/{id}", consumes = {"application/json", "application/x-www-form-urlencoded"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 부분 수정", 
      description = "Todo를 부분 수정합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 부분 수정 정보 (수정할 필드만 포함)",
          required = true,
          content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = UpdateTodoRequest.class),
                  examples = @ExampleObject(
                      name = "JSON 예시",
                      value = """
                          {
                            "status": "COMPLETED",
                            "priority": 2
                          }
                          """
                  )
              ),
              @Content(
                  mediaType = "application/x-www-form-urlencoded",
                  schema = @Schema(implementation = UpdateTodoRequest.class)
              )
          }
      )
  )
  public void partialModify(@PathVariable UUID memberId, 
                           @PathVariable Long id,
                           @Valid @RequestBody UpdateTodoRequest request) {
    todoService.partialUpdateTodo(todoPresentationMapper.toCommand(request, memberId, id));
  }

  @DeleteMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 삭제", description = "특정 Todo를 삭제합니다.")
  public void remove(@PathVariable UUID memberId, @PathVariable Long id) {
    todoService.deleteTodo(TodoQuery.of(memberId, id));
  }
}
