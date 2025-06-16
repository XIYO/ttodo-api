package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import point.zzicback.todo.application.TodoService;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.domain.*;
import point.zzicback.todo.presentation.dto.*;
import point.zzicback.todo.presentation.mapper.TodoPresentationMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

  @PostMapping("/{memberId}/todos")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Todo 생성", description = "새로운 Todo를 생성합니다.")
  public void add(@PathVariable UUID memberId, 
                 @RequestParam @NotBlank @Size(max = 255)
                 @Parameter(description = "할일 제목", example = "영어 공부하기", required = true)
                 String title,
                 
                 @RequestParam(required = false) @Size(max = 1000)
                 @Parameter(description = "할일 설명", example = "토익 문제집 2장 풀기")
                 String description,
                 
                 @RequestParam(defaultValue = "IN_PROGRESS")
                 @Parameter(description = "상태", schema = @Schema(allowableValues = {"IN_PROGRESS", "COMPLETED"}))
                 TodoStatus status,
                 
                 @RequestParam(required = false)
                 @Parameter(description = "우선순위 (0:낮음, 1:보통, 2:높음)", schema = @Schema(allowableValues = {"0", "1", "2"}))
                 Integer priority,
                 
                 @RequestParam(required = false)
                 @Parameter(description = "카테고리 ID", example = "1")
                 Long categoryId,
                 
                 @RequestParam(required = false)
                 @Parameter(description = "마감일", example = "2026-01-01")
                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                 LocalDate dueDate,
                 
                 @RequestParam(defaultValue = "NONE")
                 @Parameter(description = "반복 유형", schema = @Schema(allowableValues = {"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"}))
                 RepeatType repeatType,
                 
                 @RequestParam(required = false)
                 @Parameter(description = "태그 목록 (쉼표로 구분)", example = "영어,학습")
                 String tags) {
    
    CreateTodoRequest request = new CreateTodoRequest(
        title, description, status, priority, categoryId, dueDate, repeatType,
        parseTagsFromString(tags)
    );
    todoService.createTodo(todoPresentationMapper.toCommand(request, memberId));
  }

  @PutMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 수정", description = "Todo를 전체 수정합니다.")
  public void modify(@PathVariable UUID memberId, 
                    @PathVariable Long id,
                    
                    @RequestParam @NotBlank @Size(max = 255)
                    @Parameter(description = "할일 제목", example = "영어 공부하기", required = true)
                    String title,
                    
                    @RequestParam(required = false) @Size(max = 1000)
                    @Parameter(description = "할일 설명", example = "토익 문제집 2장 풀기")
                    String description,
                    
                    @RequestParam(required = false)
                    @Parameter(description = "상태", schema = @Schema(allowableValues = {"IN_PROGRESS", "COMPLETED"}))
                    TodoStatus status,
                    
                    @RequestParam(required = false)
                    @Parameter(description = "우선순위 (0:낮음, 1:보통, 2:높음)", schema = @Schema(allowableValues = {"0", "1", "2"}))
                    Integer priority,
                    
                    @RequestParam(required = false)
                    @Parameter(description = "카테고리 ID", example = "1")
                    Long categoryId,
                    
                    @RequestParam(required = false)
                    @Parameter(description = "마감일", example = "2026-01-01")
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dueDate,
                    
                    @RequestParam(required = false)
                    @Parameter(description = "반복 유형", schema = @Schema(allowableValues = {"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"}))
                    RepeatType repeatType,
                    
                    @RequestParam(required = false)
                    @Parameter(description = "태그 목록 (쉼표로 구분)", example = "영어,학습")
                    String tags) {
    
    UpdateTodoRequest request = new UpdateTodoRequest(
        title, description, status, priority, categoryId, dueDate, repeatType,
        parseTagsFromString(tags)
    );
    todoService.updateTodo(todoPresentationMapper.toCommand(request, memberId, id));
  }

  @PatchMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 부분 수정", description = "Todo를 부분 수정합니다.")
  public void partialModify(@PathVariable UUID memberId, 
                           @PathVariable Long id,
                           
                           @RequestParam(required = false) @Size(max = 255)
                           @Parameter(description = "할일 제목", example = "영어 공부하기")
                           String title,
                           
                           @RequestParam(required = false) @Size(max = 1000)
                           @Parameter(description = "할일 설명", example = "토익 문제집 2장 풀기")
                           String description,
                           
                           @RequestParam(required = false)
                           @Parameter(description = "상태", schema = @Schema(allowableValues = {"IN_PROGRESS", "COMPLETED"}))
                           TodoStatus status,
                           
                           @RequestParam(required = false)
                           @Parameter(description = "우선순위 (0:낮음, 1:보통, 2:높음)", schema = @Schema(allowableValues = {"0", "1", "2"}))
                           Integer priority,
                           
                           @RequestParam(required = false)
                           @Parameter(description = "카테고리 ID", example = "1")
                           Long categoryId,
                           
                           @RequestParam(required = false)
                           @Parameter(description = "마감일", example = "2026-01-01")
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                           LocalDate dueDate,
                           
                           @RequestParam(required = false)
                           @Parameter(description = "반복 유형", schema = @Schema(allowableValues = {"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"}))
                           RepeatType repeatType,
                           
                           @RequestParam(required = false)
                           @Parameter(description = "태그 목록 (쉼표로 구분)", example = "영어,학습")
                           String tags) {
    
    UpdateTodoRequest request = new UpdateTodoRequest(
        title, description, status, priority, categoryId, dueDate, repeatType,
        parseTagsFromString(tags)
    );
    todoService.partialUpdateTodo(todoPresentationMapper.toCommand(request, memberId, id));
  }

  @DeleteMapping("/{memberId}/todos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 삭제", description = "특정 Todo를 삭제합니다.")
  public void remove(@PathVariable UUID memberId, @PathVariable Long id) {
    todoService.deleteTodo(TodoQuery.of(memberId, id));
  }
  
  private Set<String> parseTagsFromString(String tags) {
    if (tags == null || tags.trim().isEmpty()) {
      return null;
    }
    return Arrays.stream(tags.split(","))
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .collect(Collectors.toSet());
  }
}
