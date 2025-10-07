package point.ttodoApi.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.todo.application.TodoInstanceService;
import point.ttodoApi.todo.application.command.CreateTodoInstanceCommand;
import point.ttodoApi.todo.application.command.UpdateTodoInstanceCommand;
import point.ttodoApi.todo.application.command.UpdateTodoStatusCommand;
import point.ttodoApi.todo.domain.TodoInstance;
import point.ttodoApi.todo.presentation.dto.request.CreateTodoInstanceRequest;
import point.ttodoApi.todo.presentation.dto.request.UpdateTodoInstanceRequest;
import point.ttodoApi.todo.presentation.dto.request.UpdateTodoStatusRequest;
import point.ttodoApi.todo.presentation.dto.response.TodoInstanceResponse;
import point.ttodoApi.todo.presentation.mapper.TodoPresentationMapper;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "투두 인스턴스", description = "투두 인스턴스(실제 실행) 관리 API")
@RestController
@RequestMapping("/todo-instances")
@RequiredArgsConstructor
@Validated
public class TodoInstanceController {

  private final TodoInstanceService instanceService;
  private final TodoPresentationMapper mapper;

  @Operation(summary = "투두 인스턴스 생성", description = "새로운 투두 인스턴스를 생성합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse createInstance(
      @AuthenticationPrincipal UUID userId,
      @Valid @ModelAttribute CreateTodoInstanceRequest request
  ) {
    log.info("Creating todo instance for user: {} with request: {}", userId, request);

    CreateTodoInstanceCommand command = mapper.toCreateCommand(request);
    TodoInstance instance = instanceService.createInstance(userId, command);

    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 인스턴스 수정", description = "기존 투두 인스턴스를 수정합니다.")
  @PutMapping("/{instanceId}")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse updateInstance(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId,
      @Valid @ModelAttribute UpdateTodoInstanceRequest request
  ) {
    log.info("Updating todo instance: {} for user: {}", instanceId, userId);

    UpdateTodoInstanceCommand command = mapper.toUpdateCommand(request);
    TodoInstance instance = instanceService.updateInstance(userId, instanceId, command);

    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 상태 변경", description = "투두 인스턴스의 상태를 변경합니다.")
  @PatchMapping("/{instanceId}/status")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse updateStatus(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId,
      @Valid @ModelAttribute UpdateTodoStatusRequest request
  ) {
    log.info("Updating todo status: {} to {} for user: {}", instanceId, request.getStatusId(), userId);

    UpdateTodoStatusCommand command = mapper.toStatusCommand(request);
    TodoInstance instance = instanceService.updateStatus(userId, instanceId, command);

    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 완료", description = "투두 인스턴스를 완료 상태로 변경합니다.")
  @PostMapping("/{instanceId}/complete")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse completeTodo(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId,
      @RequestParam(required = false) Integer completionRate,
      @RequestParam(required = false) Integer actualDuration,
      @RequestParam(required = false) String notes
  ) {
    log.info("Completing todo: {} for user: {}", instanceId, userId);

    UpdateTodoStatusCommand command = UpdateTodoStatusCommand.builder()
        .statusId(2) // 완료
        .completionRate(completionRate != null ? completionRate : 100)
        .actualDuration(actualDuration)
        .notes(notes)
        .build();

    TodoInstance instance = instanceService.updateStatus(userId, instanceId, command);
    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 진행 시작", description = "투두 인스턴스를 진행중 상태로 변경합니다.")
  @PostMapping("/{instanceId}/start")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse startTodo(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId
  ) {
    log.info("Starting todo: {} for user: {}", instanceId, userId);

    UpdateTodoStatusCommand command = UpdateTodoStatusCommand.builder()
        .statusId(1) // 진행중
        .build();

    TodoInstance instance = instanceService.updateStatus(userId, instanceId, command);
    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 건너뛰기", description = "투두 인스턴스를 건너뛴 상태로 변경합니다.")
  @PostMapping("/{instanceId}/skip")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse skipTodo(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId,
      @RequestParam(required = false) String notes
  ) {
    log.info("Skipping todo: {} for user: {}", instanceId, userId);

    UpdateTodoStatusCommand command = UpdateTodoStatusCommand.builder()
        .statusId(3) // 건너뜀
        .notes(notes)
        .build();

    TodoInstance instance = instanceService.updateStatus(userId, instanceId, command);
    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 고정/해제", description = "투두 인스턴스를 고정하거나 해제합니다.")
  @PatchMapping("/{instanceId}/pin")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse togglePin(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId,
      @RequestParam boolean pin
  ) {
    log.info("Toggling pin for todo: {} to {} for user: {}", instanceId, pin, userId);
    TodoInstance instance = instanceService.togglePin(userId, instanceId, pin);
    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 인스턴스 삭제", description = "투두 인스턴스를 소프트 삭제합니다.")
  @DeleteMapping("/{instanceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  public void deleteInstance(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId
  ) {
    log.info("Deleting todo instance: {} for user: {}", instanceId, userId);
    instanceService.deleteInstance(userId, instanceId);
  }

  @Operation(summary = "투두 인스턴스 복구", description = "소프트 삭제된 투두 인스턴스를 복구합니다.")
  @PostMapping("/{instanceId}/restore")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse restoreInstance(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId
  ) {
    log.info("Restoring todo instance: {} for user: {}", instanceId, userId);
    TodoInstance instance = instanceService.restoreInstance(userId, instanceId);
    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "투두 인스턴스 조회", description = "특정 투두 인스턴스를 조회합니다.")
  @GetMapping("/{instanceId}")
  @PreAuthorize("hasRole('USER')")
  public TodoInstanceResponse getInstance(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID instanceId
  ) {
    log.debug("Getting todo instance: {} for user: {}", instanceId, userId);
    TodoInstance instance = instanceService.getInstance(userId, instanceId);
    return mapper.toInstanceResponse(instance);
  }

  @Operation(summary = "정의별 인스턴스 목록", description = "특정 투두 정의의 모든 인스턴스를 조회합니다.")
  @GetMapping("/by-definition/{definitionId}")
  @PreAuthorize("hasRole('USER')")
  public Page<TodoInstanceResponse> getDefinitionInstances(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID definitionId,
      @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    log.debug("Getting instances for definition: {} and user: {}", definitionId, userId);
    Page<TodoInstance> instances = instanceService.getDefinitionInstances(userId, definitionId, pageable);
    return instances.map(mapper::toInstanceResponse);
  }

  @Operation(summary = "날짜별 인스턴스 목록", description = "특정 날짜의 모든 인스턴스를 조회합니다.")
  @GetMapping("/by-date")
  @PreAuthorize("hasRole('USER')")
  public List<TodoInstanceResponse> getInstancesByDate(
      @AuthenticationPrincipal UUID userId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    log.debug("Getting instances for date: {} and user: {}", date, userId);
    List<TodoInstance> instances = instanceService.getInstancesByDate(userId, date);
    return instances.stream()
        .map(mapper::toInstanceResponse)
        .toList();
  }

  @Operation(summary = "날짜 범위별 인스턴스 목록", description = "날짜 범위 내의 모든 인스턴스를 조회합니다.")
  @GetMapping("/by-date-range")
  @PreAuthorize("hasRole('USER')")
  public List<TodoInstanceResponse> getInstancesByDateRange(
      @AuthenticationPrincipal UUID userId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    log.debug("Getting instances for date range: {} - {} and user: {}", startDate, endDate, userId);
    List<TodoInstance> instances = instanceService.getInstancesByDateRange(userId, startDate, endDate);
    return instances.stream()
        .map(mapper::toInstanceResponse)
        .toList();
  }

  @Operation(summary = "상태별 인스턴스 목록", description = "특정 상태의 모든 인스턴스를 조회합니다.")
  @GetMapping("/by-status/{statusId}")
  @PreAuthorize("hasRole('USER')")
  public Page<TodoInstanceResponse> getInstancesByStatus(
      @AuthenticationPrincipal UUID userId,
      @PathVariable Integer statusId,
      @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    log.debug("Getting instances with status: {} for user: {}", statusId, userId);
    Page<TodoInstance> instances = instanceService.getInstancesByStatus(userId, statusId, pageable);
    return instances.map(mapper::toInstanceResponse);
  }

  @Operation(summary = "인스턴스 일괄 생성", description = "반복 규칙에 따라 여러 인스턴스를 일괄 생성합니다.")
  @PostMapping("/batch-generate")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public List<TodoInstanceResponse> generateInstances(
      @AuthenticationPrincipal UUID userId,
      @RequestParam UUID definitionId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    log.info("Generating instances for definition: {} from {} to {} for user: {}",
        definitionId, startDate, endDate, userId);

    List<TodoInstance> instances = instanceService.generateInstances(userId, definitionId, startDate, endDate);
    return instances.stream()
        .map(mapper::toInstanceResponse)
        .toList();
  }
}