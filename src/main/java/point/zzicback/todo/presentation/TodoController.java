package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.todo.application.TodoOriginalService;
import point.zzicback.todo.application.VirtualTodoService;
import point.zzicback.todo.application.dto.command.DeleteRepeatTodoCommand;
import point.zzicback.todo.application.dto.query.TodoQuery;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.presentation.dto.*;
import point.zzicback.todo.presentation.dto.response.CalendarTodoStatusResponse;
import point.zzicback.todo.presentation.mapper.TodoPresentationMapper;

import java.util.List;

import java.util.List;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@Tag(name = "Todo", description = "할일 관리 API")
public class TodoController {
  private final TodoOriginalService todoOriginalService;
  private final VirtualTodoService virtualTodoService;
  private final TodoPresentationMapper todoPresentationMapper;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 목록 조회", description = "사용자의 Todo 목록을 조회합니다. 다양한 조건으로 필터링이 가능합니다.")
  public Page<TodoResponse> getAll(
          @AuthenticationPrincipal MemberPrincipal principal,
          @ParameterObject @Valid TodoSearchRequest req) {
      TodoSearchQuery query = todoPresentationMapper.toQuery(req, principal.id());
      return virtualTodoService.getTodoList(query).map(todoPresentationMapper::toResponse);
  }

  @GetMapping("/{id:\\d+}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 상세 조회", description = "특정 Todo의 상세 정보를 조회합니다.")
  public TodoResponse getTodo(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long id) {
    return todoPresentationMapper.toResponse(todoOriginalService.getTodo(TodoQuery.of(principal.id(), id)));
  }

  @PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Todo 생성", description = "새로운 Todo를 생성합니다.")
  public void add(@AuthenticationPrincipal MemberPrincipal principal,
                  @Valid CreateTodoRequest request) {
    todoOriginalService.createTodo(todoPresentationMapper.toCommand(request, principal.id()));
  }

  @PutMapping(value = "/{id:\\d+}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 수정", description = "Todo를 전체 수정합니다.")
  public void modify(@AuthenticationPrincipal MemberPrincipal principal,
                     @PathVariable Long id,
                     @Valid UpdateTodoRequest request) {
    todoOriginalService.updateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @PatchMapping(
      value = "/{id:\\d+}",
      consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 부분 수정", description = "기존 Todo를 부분 수정합니다.")
  public void patchTodo(@AuthenticationPrincipal MemberPrincipal principal,
                        @PathVariable Long id,
                        @Valid UpdateTodoRequest request) {
    todoOriginalService.partialUpdateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
  }

  @PatchMapping(
      value = "/{patternId:\\d+}:{daysDifference:\\d+}",
      consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "가상 Todo 수정/생성", description = "가상 ID로 Todo를 수정합니다. 없으면 새로 생성하고, 있으면 수정합니다.")
  public void patchVirtualTodo(@AuthenticationPrincipal MemberPrincipal principal,
                               @PathVariable Long patternId,
                               @PathVariable Long daysDifference,
                               @Valid UpdateTodoRequest request) {
    String virtualId = patternId + ":" + daysDifference;
    virtualTodoService.updateOrCreateVirtualTodo(todoPresentationMapper.toVirtualCommand(request, principal.id(), virtualId));
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
    
    virtualTodoService.deleteRepeatTodo(new DeleteRepeatTodoCommand(
        principal.id(), 
        patternId, 
        daysDifference
    ));
  }

  @DeleteMapping("/{id:\\d+}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 삭제", description = "특정 Todo를 삭제합니다.")
  public void remove(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long id) {
    todoOriginalService.deleteTodo(TodoQuery.of(principal.id(), id));
  }

  @GetMapping("/calendar/monthly")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "월별 Todo 현황 조회", description = "캘린더에 표시할 월별 Todo 존재 여부를 조회합니다.")
  public List<CalendarTodoStatusResponse> getMonthlyTodoStatus(
          @AuthenticationPrincipal MemberPrincipal principal,
          @RequestParam @Schema(description = "연도", example = "2025") int year,
          @RequestParam @Schema(description = "월", example = "6") int month) {
    return virtualTodoService.getMonthlyTodoStatus(principal.id(), year, month);
  }
}
