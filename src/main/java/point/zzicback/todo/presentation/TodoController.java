package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.todo.application.TodoService;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.command.CompleteVirtualTodoCommand;
import point.zzicback.todo.application.dto.command.DeleteRepeatTodoCommand;
import point.zzicback.todo.presentation.dto.*;
import point.zzicback.todo.presentation.mapper.TodoPresentationMapper;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@Tag(name = "Todo", description = "할일 관리 API")
public class TodoController {
  private final TodoService todoService;
  private final TodoPresentationMapper todoPresentationMapper;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 목록 조회", description = "사용자의 Todo 목록을 조회합니다. 다양한 조건으로 필터링이 가능합니다.")
  public Page<TodoResponse> getAll(
          @AuthenticationPrincipal MemberPrincipal principal,
          @ParameterObject @Valid TodoSearchRequest req) {
      TodoSearchQuery query = todoPresentationMapper.toQuery(req, principal.id());
      return todoService.getTodoList(query).map(todoPresentationMapper::toResponse);
  }

  @GetMapping("/{id:\\d+}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 상세 조회", description = "특정 Todo의 상세 정보를 조회합니다.")
  public TodoResponse getTodo(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long id) {
    return todoPresentationMapper.toResponse(todoService.getTodo(TodoQuery.of(principal.id(), id)));
  }

  @GetMapping("/{patternId:\\d+}:{daysDifference:\\d+}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "가상 Todo 조회", description = "반복 Todo의 특정 가상 인스턴스를 조회합니다.")
  public TodoResponse getVirtualTodo(@AuthenticationPrincipal MemberPrincipal principal,
                                     @PathVariable Long patternId,
                                     @PathVariable Long daysDifference) {
    return todoPresentationMapper.toResponse(
        todoService.getVirtualTodo(
            VirtualTodoQuery.of(principal.id(), patternId, daysDifference)
        )
    );
  }

  @PostMapping(
    consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }
  )
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Todo 생성", 
      description = "새로운 Todo를 생성합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = {
              @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = CreateTodoRequest.class)),
              @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = CreateTodoRequest.class))
          }
      )
  )
  public void add(@AuthenticationPrincipal MemberPrincipal principal,
                  @Valid CreateTodoRequest request) {
    todoService.createTodo(todoPresentationMapper.toCommand(request, principal.id()));
  }


  @PutMapping(value = "/{id:\\d+}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 수정", 
      description = "Todo를 전체 수정합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = {
              @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = UpdateTodoRequest.class)),
              @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = UpdateTodoRequest.class))
          }
      )
  )
  public void modify(@AuthenticationPrincipal MemberPrincipal principal,
                     @PathVariable Long id,
                     @Valid UpdateTodoRequest request) {
    todoService.updateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @PatchMapping(
      value = "/{id:\\d+}",
      consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Todo 부분 수정", 
      description = "기존 Todo를 부분 수정합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = {
              @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = UpdateTodoRequest.class)),
              @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = UpdateTodoRequest.class))
          }
      )
  )
  public void patchTodo(@AuthenticationPrincipal MemberPrincipal principal,
                        @PathVariable Long id,
                        @Valid UpdateTodoRequest request) {
    todoService.partialUpdateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @PatchMapping("/{patternId:\\d+}:{daysDifference:\\d+}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "가상 Todo 완료", 
      description = "반복 Todo의 특정 가상 인스턴스를 완료 처리합니다."
  )
  public void patchVirtualTodo(@AuthenticationPrincipal MemberPrincipal principal,
                               @PathVariable Long patternId,
                               @PathVariable Long daysDifference) {
    
    todoService.completeVirtualTodo(new CompleteVirtualTodoCommand(
        principal.id(), 
        patternId, 
        daysDifference
    ));
  }

  @DeleteMapping("/{patternId:\\d+}:{daysDifference:\\d+}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "반복 Todo 삭제", 
      description = "특정 날짜부터 반복을 중단합니다. 원본 Todo와 이미 완료된 Todo는 유지됩니다."
  )
  public void deleteRepeatTodo(@AuthenticationPrincipal MemberPrincipal principal,
                               @PathVariable Long patternId,
                               @PathVariable Long daysDifference) {
    
    todoService.deleteRepeatTodo(new DeleteRepeatTodoCommand(
        principal.id(), 
        patternId, 
        daysDifference
    ));
  }

  @DeleteMapping("/{id:\\d+}")
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

  @GetMapping("/calendar/monthly")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "월별 Todo 현황 조회", description = "캘린더에 표시할 월별 Todo 존재 여부를 조회합니다.")
  public Page<CalendarTodoStatusResponse> getMonthlyTodoStatus(
          @AuthenticationPrincipal MemberPrincipal principal,
          @RequestParam @Schema(description = "연도", example = "2025") int year,
          @RequestParam @Schema(description = "월", example = "6") int month,
          @ParameterObject Pageable pageable) {
    CalendarQuery query = CalendarQuery.of(principal.id(), year, month);
    return todoService.getMonthlyTodoStatus(query, pageable)
            .map(todoPresentationMapper::toCalendarResponse);
  }
}
