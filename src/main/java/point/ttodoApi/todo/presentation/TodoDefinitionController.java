package point.ttodoApi.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.todo.application.TodoDefinitionService;
import point.ttodoApi.todo.application.command.CreateTodoDefinitionCommand;
import point.ttodoApi.todo.application.command.UpdateTodoDefinitionCommand;
import point.ttodoApi.todo.domain.TodoDefinition;
import point.ttodoApi.todo.presentation.dto.request.CreateTodoDefinitionRequest;
import point.ttodoApi.todo.presentation.dto.request.UpdateTodoDefinitionRequest;
import point.ttodoApi.todo.presentation.dto.response.TodoDefinitionResponse;
import point.ttodoApi.todo.presentation.mapper.TodoPresentationMapper;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "투두 정의", description = "투두 정의(템플릿) 관리 API")
@RestController
@RequestMapping("/todo-definitions")
@RequiredArgsConstructor
@Validated
public class TodoDefinitionController {

  private final TodoDefinitionService definitionService;
  private final TodoPresentationMapper mapper;

  @Operation(summary = "투두 정의 생성", description = "새로운 투두 정의를 생성합니다. 반복 규칙이 없으면 1회용 투두입니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public TodoDefinitionResponse createDefinition(
      @AuthenticationPrincipal UUID userId,
      @Valid @ModelAttribute CreateTodoDefinitionRequest request
  ) {
    log.info("Creating todo definition for user: {} with request: {}", userId, request);

    CreateTodoDefinitionCommand command = mapper.toCreateCommand(request);
    TodoDefinition definition = definitionService.createDefinition(userId, command);

    return mapper.toDefinitionResponse(definition);
  }

  @Operation(summary = "투두 정의 수정", description = "기존 투두 정의를 수정합니다.")
  @PutMapping("/{definitionId}")
  @PreAuthorize("hasRole('USER')")
  public TodoDefinitionResponse updateDefinition(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID definitionId,
      @Valid @ModelAttribute UpdateTodoDefinitionRequest request
  ) {
    log.info("Updating todo definition: {} for user: {}", definitionId, userId);

    UpdateTodoDefinitionCommand command = mapper.toUpdateCommand(request);
    TodoDefinition definition = definitionService.updateDefinition(userId, definitionId, command);

    return mapper.toDefinitionResponse(definition);
  }

  @Operation(summary = "투두 정의 삭제", description = "투두 정의를 소프트 삭제합니다.")
  @DeleteMapping("/{definitionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  public void deleteDefinition(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID definitionId
  ) {
    log.info("Deleting todo definition: {} for user: {}", definitionId, userId);
    definitionService.deleteDefinition(userId, definitionId);
  }

  @Operation(summary = "투두 정의 복구", description = "소프트 삭제된 투두 정의를 복구합니다.")
  @PostMapping("/{definitionId}/restore")
  @PreAuthorize("hasRole('USER')")
  public TodoDefinitionResponse restoreDefinition(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID definitionId
  ) {
    log.info("Restoring todo definition: {} for user: {}", definitionId, userId);
    TodoDefinition definition = definitionService.restoreDefinition(userId, definitionId);
    return mapper.toDefinitionResponse(definition);
  }

  @Operation(summary = "투두 정의 조회", description = "특정 투두 정의를 조회합니다.")
  @GetMapping("/{definitionId}")
  @PreAuthorize("hasRole('USER')")
  public TodoDefinitionResponse getDefinition(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID definitionId
  ) {
    log.debug("Getting todo definition: {} for user: {}", definitionId, userId);
    TodoDefinition definition = definitionService.getDefinition(userId, definitionId);
    return mapper.toDefinitionResponse(definition);
  }

  @Operation(summary = "투두 정의 목록", description = "사용자의 모든 투두 정의를 페이징하여 조회합니다.")
  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public Page<TodoDefinitionResponse> getUserDefinitions(
      @AuthenticationPrincipal UUID userId,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    log.debug("Getting todo definitions for user: {}", userId);
    Page<TodoDefinition> definitions = definitionService.getUserDefinitions(userId, pageable);
    return definitions.map(mapper::toDefinitionResponse);
  }

  @Operation(summary = "반복 투두 정의 목록", description = "반복 규칙이 있는 투두 정의만 조회합니다.")
  @GetMapping("/recurring")
  @PreAuthorize("hasRole('USER')")
  public List<TodoDefinitionResponse> getRecurringDefinitions(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting recurring definitions for user: {}", userId);
    List<TodoDefinition> definitions = definitionService.getRecurringDefinitions(userId);
    return definitions.stream()
        .map(mapper::toDefinitionResponse)
        .toList();
  }

  @Operation(summary = "1회용 투두 정의 목록", description = "반복 규칙이 없는 1회용 투두 정의만 조회합니다.")
  @GetMapping("/single")
  @PreAuthorize("hasRole('USER')")
  public List<TodoDefinitionResponse> getSingleDefinitions(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting single definitions for user: {}", userId);
    List<TodoDefinition> definitions = definitionService.getSingleDefinitions(userId);
    return definitions.stream()
        .map(mapper::toDefinitionResponse)
        .toList();
  }

  @Operation(summary = "투두 정의 통계", description = "사용자의 투두 정의 통계를 조회합니다.")
  @GetMapping("/stats")
  @PreAuthorize("hasRole('USER')")
  public Map<String, Long> getDefinitionStats(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting definition stats for user: {}", userId);
    return definitionService.getDefinitionStats(userId);
  }
}