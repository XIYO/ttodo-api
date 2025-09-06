package point.ttodoApi.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.auth.domain.MemberPrincipal;
import point.ttodoApi.shared.validation.*;
import point.ttodoApi.todo.application.*;
import point.ttodoApi.todo.application.command.DeleteTodoCommand;
import point.ttodoApi.todo.application.query.*;
import point.ttodoApi.todo.application.result.TodoStatistics;
import point.ttodoApi.todo.domain.TodoId;
import point.ttodoApi.todo.presentation.dto.*;
import point.ttodoApi.todo.presentation.mapper.TodoPresentationMapper;

import java.time.LocalDate;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@Tag(name = "할 일(Todo) 핵심 기능", description = "ttodo 서비스의 핵심 기능인 할 일 관리 API를 제공합니다. 할 일 생성/조회/수정/삭제는 물론, 반복 스케줄링, 완료 처리, 상단 고정, 캘린더 통합, 통계 조회 등 다양한 기능을 지원합니다.\n\n" +
        "할 일 ID 형식: `{id}:{daysDifference}` \n" +
        "- id: 원본 할 일 ID\n" +
        "- daysDifference: 기준일로부터의 일수 차이 (0 = 원본, 1 = 1일 후, -1 = 1일 전)")
public class TodoController {
  private final TodoTemplateService todoTemplateService;
  private final TodoInstanceService todoInstanceService;
  private final TodoSearchService todoSearchService;
  private final TodoPresentationMapper todoPresentationMapper;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ValidPageable(sortFields = SortFieldsProvider.TODO)
  @Operation(
          summary = "할 일 목록 조회",
          description = "사용자의 할 일 목록을 페이지네이션과 함께 조회합니다. 다양한 필터링 옵션을 제공하며, 반복 할 일의 가상 인스턴스도 포함합니다.\n\n" +
                  "필터 옵션:\n" +
                  "- date: 특정 날짜의 할 일만 조회\n" +
                  "- categoryId: 특정 카테고리의 할 일만 조회\n" +
                  "- completed: 완료 상태로 필터링\n" +
                  "- pinned: 상단 고정 상태로 필터링\n" +
                  "- keyword: 제목/메모 검색\n" +
                  "- tags: 태그로 필터링\n\n" +
                  "정렬 옵션: pinOrder(asc/desc), createdAt(asc/desc), updatedAt(asc/desc)"
  )
  @ApiResponse(
          responseCode = "200",
          description = "할 일 목록 조회 성공",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                          schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TodoResponse.class)
                  )
          )
  )
  @ApiResponse(
          responseCode = "400",
          description = "요청 파라미터 오류",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  public Page<TodoResponse> getAll(
          @AuthenticationPrincipal MemberPrincipal principal,
          @ParameterObject @Valid TodoSearchRequest req) {
    TodoSearchQuery query = todoPresentationMapper.toQuery(req, principal.id());
    return todoInstanceService.getTodoList(query).map(todoPresentationMapper::toResponse);
  }

  @GetMapping("/{id:\\d+}:{daysDifference:\\d+}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
          summary = "할 일 상세 조회",
          description = "특정 할 일의 상세 정보를 조회합니다. ID 형식에 따라 원본 할 일 또는 가상 할 일을 조회할 수 있습니다.\n\n" +
                  "예시:\n" +
                  "- GET /todos/82:0 (원본 할 일 조회)\n" +
                  "- GET /todos/82:1 (1일 후 가상 할 일 조회)\n" +
                  "- GET /todos/82:7 (7일 후 가상 할 일 조회)"
  )
  @ApiResponse(
          responseCode = "200",
          description = "할 일 상세 조회 성공",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TodoResponse.class)
          )
  )
  @ApiResponse(
          responseCode = "403",
          description = "다른 사용자의 할 일에 접근 시도",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @ApiResponse(
          responseCode = "404",
          description = "할 일을 찾을 수 없음",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  public TodoResponse getTodo(@AuthenticationPrincipal MemberPrincipal principal,
                              @PathVariable Long id,
                              @PathVariable Long daysDifference) {
    if (daysDifference == 0) {
      // 원본 TodoTemplate 조회 (82:0)
      return todoPresentationMapper.toResponse(todoTemplateService.getTodo(TodoQuery.of(principal.id(), id)));
    } else {
      // 가상 Todo 조회 (82:1, 82:2, ...)
      VirtualTodoQuery query = VirtualTodoQuery.of(principal.id(), id, daysDifference);
      return todoPresentationMapper.toResponse(todoInstanceService.getVirtualTodo(query));
    }
  }

  @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
          summary = "할 일 생성",
          description = "새로운 할 일을 생성합니다. 카테고리, 우선순위, 태그, 반복 설정 등 다양한 옵션을 설정할 수 있습니다. 입력은 form만 지원합니다(application/x-www-form-urlencoded 또는 multipart/form-data).\n\n" +
                  "필수 필드:\n" +
                  "- title: 할 일 제목\n\n" +
                  "선택 필드:\n" +
                  "- date: 할 일 날짜 (YYYY-MM-DD)\n" +
                  "- categoryId: 카테고리 ID\n" +
                  "- priority: 우선순위 (HIGH, MEDIUM, LOW)\n" +
                  "- tags: 태그 목록\n" +
                  "- recurrenceRuleJson: RRULE 기반 반복 규칙(JSON 문자열) — form 필드에 문자열로 넣어주세요.",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                  required = true,
                  content = {
                          @io.swagger.v3.oas.annotations.media.Content(
                                  mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateTodoRequest.class),
                                  examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                          name = "폼 전송 예시",
                                          summary = "application/x-www-form-urlencoded",
                                          value = """
                                                  {
                                                    "title": "주 2회 운동",
                                                    "priorityId": 1,
                                                    "date": "2025-01-01",
                                                    "recurrenceRuleJson": "{\\\"frequency\\\":\\\"WEEKLY\\\",\\\"interval\\\":1,\\\"byWeekDays\\\":[\\\"MO\\\",\\\"TH\\\"],\\\"endCondition\\\":{\\\"type\\\":\\\"UNTIL\\\",\\\"until\\\":\\\"2025-12-31\\\"},\\\"anchorDate\\\":\\\"2025-01-01\\\",\\\"timezone\\\":\\\"Asia/Seoul\\\"}}"
                                                  }
                                                  """
                                  )
                          ),
                          @io.swagger.v3.oas.annotations.media.Content(
                                  mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateTodoRequest.class),
                                  examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                          name = "멀티파트 전송 예시",
                                          summary = "multipart/form-data",
                                          value = """
                                                  {
                                                    "title": "문서 작성",
                                                    "description": "상세 설명",
                                                    "recurrenceRuleJson": "{\\\"frequency\\\":\\\"DAILY\\\",\\\"interval\\\":1,\\\"endCondition\\\":{\\\"type\\\":\\\"COUNT\\\",\\\"count\\\":10}}"
                                                  }
                                                  """
                                  )
                          )
                  }
          )
  )
  @ApiResponse(responseCode = "201", description = "할 일 생성 성공")
  @ApiResponse(
          responseCode = "400",
          description = "입력값 검증 실패",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @ApiResponse(
          responseCode = "404",
          description = "유효하지 않은 카테고리 ID",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  public void add(@AuthenticationPrincipal MemberPrincipal principal,
                  @Valid CreateTodoRequest request) {
    todoTemplateService.createTodo(todoPresentationMapper.toCommand(request, principal.id()));
  }

  @PutMapping(value = "/{id:\\d+}:{daysDifference:\\d+}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
          summary = "할 일 전체 수정 (PUT)",
          description = "할 일의 모든 필드를 전체 수정합니다. 원본 할 일 수정 시 모든 가상 인스턴스에 영향을 줍니다. 가상 할 일 수정 시 해당 날짜의 할 일만 수정됩니다. 입력은 form만 지원합니다.\n\n" +
                  "예시:\n" +
                  "- PUT /todos/82:0 (원본 할 일 수정 - 모든 가상 인스턴스에 반영)\n" +
                  "- PUT /todos/82:1 (1일 후 가상 할 일만 수정)",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                  required = true,
                  content = {
                          @io.swagger.v3.oas.annotations.media.Content(
                                  mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateTodoRequest.class),
                                  examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                          name = "폼 전송 예시",
                                          value = """
                                                  {
                                                    "title": "주 3회 운동",
                                                    "recurrenceRuleJson": "{\\\"frequency\\\":\\\"WEEKLY\\\",\\\"interval\\\":1,\\\"byWeekDays\\\":[\\\"MO\\\",\\\"WE\\\",\\\"FR\\\"],\\\"endCondition\\\":{\\\"type\\\":\\\"COUNT\\\",\\\"count\\\":24}}"
                                                  }
                                                  """
                                  )
                          ),
                          @io.swagger.v3.oas.annotations.media.Content(
                                  mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateTodoRequest.class)
                          )
                  }
          )
  )
  @ApiResponse(responseCode = "204", description = "할 일 수정 성공")
  @ApiResponse(
          responseCode = "400",
          description = "입력값 검증 실패",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @ApiResponse(
          responseCode = "403",
          description = "다른 사용자의 할 일에 접근 시도",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @ApiResponse(
          responseCode = "404",
          description = "할 일을 찾을 수 없음",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @PreAuthorize("@todoTemplateService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void modify(@AuthenticationPrincipal MemberPrincipal principal,
                     @PathVariable Long id,
                     @PathVariable Long daysDifference,
                     @Valid UpdateTodoRequest request) {
    if (daysDifference == 0) {
      // 원본 TodoTemplate 수정
      todoTemplateService.updateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
    } else {
      // 가상 Todo 수정/생성
      String virtualId = id + ":" + daysDifference;
      todoInstanceService.updateOrCreateVirtualTodo(todoPresentationMapper.toVirtualCommand(request, principal.id(), virtualId));
    }
  }

  @PatchMapping(
          value = "/{id:\\d+}:{daysDifference:\\d+}",
          consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
          summary = "할 일 부분 수정 (PATCH)",
          description = "할 일의 특정 필드만 선택적으로 수정합니다. 주로 완료 상태 토글이나 긴급한 정보 업데이트에 사용됩니다. 입력은 form만 지원합니다.\n\n" +
                  "특징:\n" +
                  "- completed 필드만 수정 시: 해당 날짜의 가상 할 일만 업데이트\n" +
                  "- isPinned 필드: 상단 고정 상태 토글\n" +
                  "- displayOrder 필드: 표시 순서 변경\n" +
                  "- 다른 필드 포함 시: 원본 또는 가상 할 일 업데이트\n\n" +
                  "사용 예:\n" +
                  "- PATCH /todos/82:3 {\"completed\": true} (3일 후 할 일만 완료 처리)\n" +
                  "- PATCH /todos/82:0 {\"title\": \"새 제목\"} (원본 제목 수정)\n" +
                  "- PATCH /todos/82:0 {\"isPinned\": true} (상단 고정)\n" +
                  "- PATCH /todos/82:0 {\"displayOrder\": 5} (순서 변경)"
  )
  @ApiResponse(responseCode = "204", description = "할 일 부분 수정 성공")
  @ApiResponse(
          responseCode = "400",
          description = "입력값 검증 실패",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @ApiResponse(
          responseCode = "403",
          description = "다른 사용자의 할 일에 접근 시도",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @ApiResponse(
          responseCode = "404",
          description = "할 일을 찾을 수 없음",
          content = @io.swagger.v3.oas.annotations.media.Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = point.ttodoApi.shared.error.ErrorResponse.class)
          )
  )
  @PreAuthorize("@todoTemplateService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void patchTodo(@AuthenticationPrincipal MemberPrincipal principal,
                        @PathVariable Long id,
                        @PathVariable Long daysDifference,
                        @Valid UpdateTodoRequest request) {
    // Handle special fields first (isPinned, displayOrder)
    // TODO: These fields are not present in UpdateTodoRequest
    // if (request.displayOrder() != null) {
    //   todoTemplateService.changeOrder(principal.id(), id, request.displayOrder());
    //   return;
    // }
    // 
    // if (request.isPinned() != null) {
    //   todoTemplateService.togglePin(TodoQuery.of(principal.id(), id));
    //   return;
    // }

    // 완료 상태만 수정하는 경우
    if (todoPresentationMapper.isOnlyCompleteFieldUpdate(request)) {
      String virtualId = id + ":" + daysDifference;
      todoInstanceService.updateOrCreateVirtualTodo(todoPresentationMapper.toVirtualCommand(request, principal.id(), virtualId));
    } else {
      // 다른 필드 수정하는 경우 - Todo 테이블에 데이터가 있는지 먼저 확인
      TodoId todoId = new TodoId(id, daysDifference);
      if (todoInstanceService.existsVirtualTodo(principal.id(), todoId)) {
        // Todo 테이블에 데이터가 있으면 가상 Todo 수정
        String virtualId = id + ":" + daysDifference;
        todoInstanceService.updateOrCreateVirtualTodo(todoPresentationMapper.toVirtualCommand(request, principal.id(), virtualId));
      } else {
        // Todo 테이블에 데이터가 없으면 원본 TodoTemplate 수정
        todoTemplateService.partialUpdateTodo(todoPresentationMapper.toCommand(request, principal.id(), id));
      }
    }
  }

  @DeleteMapping(value = "/{id:\\d+}:{daysDifference:\\d+}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
          summary = "할 일 삭제/숨기기",
          description = "할 일을 삭제하거나 숨깁니다. 반복 할 일의 경우 선택적으로 모든 인스턴스를 삭제하거나 특정 날짜만 숨길 수 있습니다.\n\n" +
                  "삭제 옵션:\n" +
                  "- deleteAll=true: 원본과 모든 가상 인스턴스 삭제\n" +
                  "- deleteAll=false: 해당 날짜의 할 일만 숨김\n\n" +
                  "예시:\n" +
                  "- DELETE /todos/82:0?deleteAll=true (모든 반복 인스턴스 삭제)\n" +
                  "- DELETE /todos/82:3?deleteAll=false (3일 후 할 일만 숨김)"
  )
  @ApiResponse(responseCode = "204", description = "할 일 삭제/숨기기 성공")
  @ApiResponse(responseCode = "403", description = "다른 사용자의 할 일에 접근 시도")
  @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음")
  @PreAuthorize("@todoTemplateService.isOwnerWithDaysDifference(#id, #daysDifference, authentication.principal.id)")
  public void remove(@AuthenticationPrincipal MemberPrincipal principal,
                     @PathVariable Long id,
                     @PathVariable Long daysDifference,
                     @Valid DeleteTodoRequest request) {
    if (request.deleteAll()) {
      // deleteAll이 true이면 원본 TodoTemplate을 비활성화하여 모든 가상 투두도 숨김
      todoTemplateService.deactivateTodo(new DeleteTodoCommand(
              principal.id(),
              id,
              daysDifference,
              true
      ));
    } else {
      // deleteAll이 false이면 해당 날짜의 투두만 숨김
      // daysDifference가 0이든 아니든 모두 가상 투두처럼 처리
      todoInstanceService.deactivateVirtualTodo(new DeleteTodoCommand(
              principal.id(),
              id,
              daysDifference,
              false
      ));
    }
  }


  @GetMapping("/statistics")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
          summary = "할 일 통계 조회",
          description = "특정 날짜의 할 일 통계를 조회합니다. 진행 중 및 완료된 할 일의 개수, 카테고리별 분포 등의 통계 정보를 제공합니다.\n\n" +
                  "통계 항목:\n" +
                  "- totalCount: 전체 할 일 수\n" +
                  "- completedCount: 완료된 할 일 수\n" +
                  "- progressCount: 진행 중인 할 일 수\n" +
                  "- completionRate: 완료율 (%)"
  )
  @ApiResponse(responseCode = "200", description = "통계 조회 성공")
  public TodoStatistics getTodoStatistics(
          @AuthenticationPrincipal MemberPrincipal principal,
          @RequestParam(required = false) @Schema(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-07-02") LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();
    return todoInstanceService.getTodoStatistics(principal.id(), targetDate);
  }

}
