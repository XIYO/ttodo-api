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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.todo.application.TodoOriginalService;
import point.zzicback.todo.application.VirtualTodoService;
import point.zzicback.todo.application.dto.command.DeleteTodoCommand;
import point.zzicback.todo.application.dto.query.TodoQuery;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.query.VirtualTodoQuery;
import point.zzicback.todo.application.dto.result.TodoStatistics;
import point.zzicback.todo.domain.TodoId;
import point.zzicback.todo.presentation.dto.request.*;
import point.zzicback.todo.presentation.dto.response.*;
import point.zzicback.todo.presentation.dto.response.CalendarTodoStatusResponse;
import point.zzicback.todo.presentation.mapper.TodoPresentationMapper;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@Tag(name = "할일(Todo) 관리", description = "할일 생성, 조회, 수정, 삭제, 완료 처리, 반복 스케줄링, 상단 고정, 월별 현황 조회, 통계 등 할일 관리 전반 API")
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

  @GetMapping("/{id:\\d+}:{daysDifference:\\d+}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 상세 조회", description = "특정 Todo의 상세 정보를 조회합니다.")
  public TodoResponse getTodo(@AuthenticationPrincipal MemberPrincipal principal, 
                             @PathVariable Long id, 
                             @PathVariable Long daysDifference) {
    if (daysDifference == 0) {
      // 원본 TodoOriginal 조회 (82:0)
      return todoPresentationMapper.toResponse(todoOriginalService.getTodo(TodoQuery.of(principal.id(), id)));
    } else {
      // 가상 Todo 조회 (82:1, 82:2, ...)
      VirtualTodoQuery query = VirtualTodoQuery.of(principal.id(), id, daysDifference);
      return todoPresentationMapper.toResponse(virtualTodoService.getVirtualTodo(query));
    }
  }

  @PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Todo 생성", description = "새로운 Todo를 생성합니다.")
  public void add(@AuthenticationPrincipal MemberPrincipal principal,
                  @Valid CreateTodoRequest request) {
    todoOriginalService.createTodo(todoPresentationMapper.toCommand(request, principal.id()));
  }

  @PutMapping(value = "/{id:\\d+}:{daysDifference:\\d+}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 수정", description = "Todo를 전체 수정합니다.")
  @PreAuthorize("@todoOriginalService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void modify(@AuthenticationPrincipal MemberPrincipal principal,
                     @PathVariable Long id,
                     @PathVariable Long daysDifference,
                     @Valid UpdateTodoRequest request) {
    if (daysDifference == 0) {
      // 원본 TodoOriginal 수정
      todoOriginalService.updateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
    } else {
      // 가상 Todo 수정/생성
      String virtualId = id + ":" + daysDifference;
      virtualTodoService.updateOrCreateVirtualTodo(todoPresentationMapper.toVirtualCommand(request, principal.id(), virtualId));
    }
  }

  @PatchMapping(
      value = "/{id:\\d+}:{daysDifference:\\d+}",
      consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE }
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 부분 수정", description = "기존 Todo를 부분 수정합니다.")
  @PreAuthorize("@todoOriginalService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void patchTodo(@AuthenticationPrincipal MemberPrincipal principal,
                        @PathVariable Long id,
                        @PathVariable Long daysDifference,
                        @Valid UpdateTodoRequest request) {
    // 완료 상태만 수정하는 경우
    if (todoPresentationMapper.isOnlyCompleteFieldUpdate(request)) {
      String virtualId = id + ":" + daysDifference;
      virtualTodoService.updateOrCreateVirtualTodo(todoPresentationMapper.toVirtualCommand(request, principal.id(), virtualId));
    } else {
      // 다른 필드 수정하는 경우 - Todo 테이블에 데이터가 있는지 먼저 확인
      TodoId todoId = new TodoId(id, daysDifference);
      if (virtualTodoService.existsVirtualTodo(principal.id(), todoId)) {
        // Todo 테이블에 데이터가 있으면 가상 Todo 수정
        String virtualId = id + ":" + daysDifference;
        virtualTodoService.updateOrCreateVirtualTodo(todoPresentationMapper.toVirtualCommand(request, principal.id(), virtualId));
      } else {
        // Todo 테이블에 데이터가 없으면 원본 TodoOriginal 수정
        todoOriginalService.partialUpdateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
      }
    }
  }

  @DeleteMapping(value = "/{id:\\d+}:{daysDifference:\\d+}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 삭제", description = "특정 Todo를 삭제하거나 숨깁니다.")
  @PreAuthorize("@todoOriginalService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void remove(@AuthenticationPrincipal MemberPrincipal principal, 
                     @PathVariable Long id, 
                     @PathVariable Long daysDifference,
                     @Valid DeleteTodoRequest request) {
    if (request.deleteAll()) {
      // deleteAll이 true이면 원본 TodoOriginal을 비활성화하여 모든 가상 투두도 숨김
      todoOriginalService.deactivateTodo(new DeleteTodoCommand(
          principal.id(), 
          id, 
          daysDifference,
          true
      ));
    } else {
      // deleteAll이 false이면 해당 날짜의 투두만 숨김
      // daysDifference가 0이든 아니든 모두 가상 투두처럼 처리
      virtualTodoService.deactivateVirtualTodo(new DeleteTodoCommand(
          principal.id(), 
          id, 
          daysDifference,
          false
      ));
    }
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

  @GetMapping("/statistics")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Todo 통계 조회", description = "특정 날짜의 Todo 진행중/완료 갯수를 조회합니다. 날짜를 지정하지 않으면 오늘 기준으로 조회합니다.")
  public TodoStatistics getTodoStatistics(
          @AuthenticationPrincipal MemberPrincipal principal,
          @RequestParam(required = false) @Schema(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-07-02") LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();
    return virtualTodoService.getTodoStatistics(principal.id(), targetDate);
  }

  @PatchMapping("/{id:\\d+}:{daysDifference:\\d+}/pin")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Todo 상단 고정 토글", description = "Todo의 상단 고정 상태를 토글합니다.")
  @PreAuthorize("@todoOriginalService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void togglePin(@AuthenticationPrincipal MemberPrincipal principal,
                       @PathVariable Long id,
                       @PathVariable Long daysDifference) {
    todoOriginalService.togglePin(TodoQuery.of(principal.id(), id));
  }

  @PatchMapping("/{id:\\d+}:{daysDifference:\\d+}/pin-order")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "핀 고정된 Todo 순서 변경", description = "핀 고정된 Todo의 표시 순서를 변경합니다.")
  @PreAuthorize("@todoOriginalService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void changeOrder(@AuthenticationPrincipal MemberPrincipal principal,
                         @PathVariable Long id,
                         @PathVariable Long daysDifference,
                         @Valid @RequestBody ChangeOrderRequest request) {
    todoOriginalService.changeOrder(principal.id(), id, request.newOrder());
  }
}
