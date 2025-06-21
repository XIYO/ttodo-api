package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.todo.application.TodoService;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.presentation.dto.*;
import point.zzicback.todo.presentation.mapper.TodoPresentationMapper;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {
  private final TodoService todoService;
  private final TodoPresentationMapper todoPresentationMapper;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 목록 조회", description = "사용자의 Todo 목록을 조회합니다. 다양한 조건으로 필터링이 가능합니다.")
  public Page<TodoResponse> getAll(
          @AuthenticationPrincipal MemberPrincipal principal,
          @RequestParam(required = false)
          @Parameter(description = "할일 상태 필터 (0: 진행중, 1: 완료, 2: 지연)", 
                     schema = @Schema(allowableValues = {"0", "1", "2"}),
                     example = "0")
          Integer statusId,
          @RequestParam(required = false)
          @Parameter(description = "카테고리 ID 필터", example = "1")
          Long categoryId,
          @RequestParam(required = false)
          @Parameter(description = "우선순위 필터 (0: 낮음, 1: 보통, 2: 높음)",
                     schema = @Schema(allowableValues = {"0", "1", "2"}),
                     example = "1")
          Integer priorityId,
          @RequestParam(required = false)
          @Parameter(description = "검색 시작일", example = "2024-01-01")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
          @RequestParam(required = false)
          @Parameter(description = "검색 종료일", example = "2024-01-31")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
          @RequestParam(required = false)
          @Parameter(description = "검색 키워드 (제목, 설명, 태그에서 검색)",
                     example = "영어")
          String keyword,
          @RequestParam(required = false)
          @Parameter(description = "숨길 상태 ID 목록", example = "1,2")
          List<Integer> hideStatusIds,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    TodoListQuery query = TodoListQuery.of(principal.id(), statusId, categoryId,
        priorityId, keyword, hideStatusIds, startDate, endDate, pageable);
    return todoService.getTodoList(query).map(todoPresentationMapper::toResponse);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 상세 조회", description = "특정 Todo의 상세 정보를 조회합니다.")
  public TodoResponse getTodo(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long id) {
    return todoPresentationMapper.toResponse(todoService.getTodo(TodoQuery.of(principal.id(), id)));
  }

  @PostMapping(consumes = {"application/json"})
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Todo 생성 (JSON)", 
      description = "새로운 Todo를 생성합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 생성 정보",
          required = true,
          content = @Content(
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
          )
      )
  )
  public void add(@AuthenticationPrincipal MemberPrincipal principal, 
                 @Valid @RequestBody CreateTodoRequest request) {
    todoService.createTodo(todoPresentationMapper.toCommand(request, principal.id()));
  }

  @PostMapping(consumes = {"application/x-www-form-urlencoded"})
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Todo 생성 (Form)", 
      description = "새로운 Todo를 생성합니다. 태그는 쉼표로 구분하여 입력하세요 (예: 영어,학습).",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 생성 정보",
          required = true,
          content = @Content(
              mediaType = "application/x-www-form-urlencoded",
              schema = @Schema(implementation = CreateTodoRequest.class)
          )
      )
  )
  public void addForm(@AuthenticationPrincipal MemberPrincipal principal,
                     @Valid @ModelAttribute CreateTodoRequest request) {
    todoService.createTodo(todoPresentationMapper.toCommand(request, principal.id()));
  }

  @PutMapping(value = "/{id}", consumes = {"application/json"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 수정 (JSON)", 
      description = "Todo를 전체 수정합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 수정 정보",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = UpdateTodoRequest.class),
              examples = @ExampleObject(
                  name = "JSON 예시",
                  value = """
                      {
                        "title": "영어 공부하기 (수정)",
                        "description": "토익 문제집 3장 풀어보기",
                        "statusId": 1,
                        "priorityId": 2,
                        "categoryId": 1,
                        "dueDate": "2026-01-02",
                        "repeatType": "DAILY",
                        "tags": ["영어", "학습", "토익"]
                      }
                      """
              )
          )
      )
  )
  public void modify(@AuthenticationPrincipal MemberPrincipal principal,
                    @PathVariable Long id,
                    @Valid @RequestBody UpdateTodoRequest request) {
    todoService.updateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @PutMapping(value = "/{id}", consumes = {"application/x-www-form-urlencoded"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 수정 (Form)", 
      description = "Todo를 전체 수정합니다. 태그는 쉼표로 구분하여 입력하세요 (예: 영어,학습,토익).",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 수정 정보",
          required = true,
          content = @Content(
              mediaType = "application/x-www-form-urlencoded",
              schema = @Schema(implementation = UpdateTodoRequest.class)
          )
      )
  )
  public void modifyForm(@AuthenticationPrincipal MemberPrincipal principal,
                        @PathVariable Long id,
                        @Valid @ModelAttribute UpdateTodoRequest request) {
    todoService.updateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @PatchMapping(value = "/{id}", consumes = {"application/json"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 부분 수정 (JSON)", 
      description = "Todo를 부분 수정합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 부분 수정 정보 (수정할 필드만 포함)",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = UpdateTodoRequest.class),
              examples = @ExampleObject(
                  name = "JSON 예시",
                  value = """
                      {
                        "statusId": 1,
                        "priorityId": 2
                      }
                      """
              )
          )
      )
  )
  public void partialModify(@AuthenticationPrincipal MemberPrincipal principal,
                           @PathVariable Long id,
                           @Valid @RequestBody UpdateTodoRequest request) {
    todoService.partialUpdateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @PatchMapping(value = "/{id}", consumes = {"application/x-www-form-urlencoded"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 부분 수정 (Form)", 
      description = "Todo를 부분 수정합니다. 태그는 쉼표로 구분하여 입력하세요 (예: 영어,학습,토익).",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Todo 부분 수정 정보 (수정할 필드만 포함)",
          required = true,
          content = @Content(
              mediaType = "application/x-www-form-urlencoded",
              schema = @Schema(implementation = UpdateTodoRequest.class)
          )
      )
  )
  public void partialModifyForm(@AuthenticationPrincipal MemberPrincipal principal,
                               @PathVariable Long id,
                               @Valid @ModelAttribute UpdateTodoRequest request) {
    todoService.partialUpdateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 삭제", description = "특정 Todo를 삭제합니다.")
  public void remove(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long id) {
    todoService.deleteTodo(TodoQuery.of(principal.id(), id));
  }
  
  @GetMapping("/statistics")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Todo 통계 조회", 
      description = "사용자의 Todo 상태별 통계를 조회합니다.",
      responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "통계 조회 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = TodoStatisticsResponse.class),
              examples = @ExampleObject(
                  name = "응답 예시",
                  value = """
                      {
                        "content": [
                          {
                            "statisticsName": "진행중",
                            "statisticsValue": 0
                          },
                          {
                            "statisticsName": "기간초과",
                            "statisticsValue": 4
                          },
                          {
                            "statisticsName": "완료",
                            "statisticsValue": 2
                          },
                          {
                            "statisticsName": "전체",
                            "statisticsValue": 6
                          }
                        ]
                      }
                      """
              )
          )
      )
  )
  public TodoStatisticsResponse getStatistics(@AuthenticationPrincipal MemberPrincipal principal) {
      return todoPresentationMapper.toStatisticsResponse(todoService.getTodoStatistics(principal.id()));
  }
}
