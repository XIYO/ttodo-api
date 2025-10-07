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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.todo.application.TodoViewService;
import point.ttodoApi.todo.domain.TodoView;
import point.ttodoApi.todo.presentation.dto.response.TodoViewResponse;
import point.ttodoApi.todo.presentation.mapper.TodoPresentationMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "투두 통합 조회", description = "투두 통합 조회 API - 정의와 인스턴스를 합친 읽기 전용 뷰")
@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@Validated
public class TodoViewController {

  private final TodoViewService viewService;
  private final TodoPresentationMapper mapper;

  @Operation(summary = "투두 조회", description = "특정 투두를 조회합니다.")
  @GetMapping("/{todoId}")
  @PreAuthorize("hasRole('USER')")
  public TodoViewResponse getTodo(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID todoId
  ) {
    log.debug("Getting todo: {} for user: {}", todoId, userId);
    TodoView todo = viewService.getTodo(userId, todoId);
    return mapper.toViewResponse(todo);
  }

  @Operation(summary = "모든 투두 조회", description = "사용자의 모든 투두를 페이징하여 조회합니다.")
  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public Page<TodoViewResponse> getUserTodos(
      @AuthenticationPrincipal UUID userId,
      @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    log.debug("Getting all todos for user: {}", userId);
    Page<TodoView> todos = viewService.getUserTodos(userId, pageable);
    return todos.map(mapper::toViewResponse);
  }

  @Operation(summary = "오늘의 투두", description = "오늘의 투두 목록을 조회합니다.")
  @GetMapping("/today")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getTodayTodos(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting today's todos for user: {}", userId);
    List<TodoView> todos = viewService.getTodayTodos(userId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "날짜별 투두", description = "특정 날짜 범위의 투두를 조회합니다.")
  @GetMapping("/by-date")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getTodosByDateRange(
      @AuthenticationPrincipal UUID userId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    log.debug("Getting todos for date range: {} - {} for user: {}", startDate, endDate, userId);
    List<TodoView> todos = viewService.getTodosByDateRange(userId, startDate, endDate);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "미완료 투두", description = "미완료 상태의 투두를 조회합니다.")
  @GetMapping("/pending")
  @PreAuthorize("hasRole('USER')")
  public Page<TodoViewResponse> getPendingTodos(
      @AuthenticationPrincipal UUID userId,
      @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    log.debug("Getting pending todos for user: {}", userId);
    Page<TodoView> todos = viewService.getPendingTodos(userId, pageable);
    return todos.map(mapper::toViewResponse);
  }

  @Operation(summary = "완료된 투두", description = "완료 상태의 투두를 조회합니다.")
  @GetMapping("/completed")
  @PreAuthorize("hasRole('USER')")
  public Page<TodoViewResponse> getCompletedTodos(
      @AuthenticationPrincipal UUID userId,
      @PageableDefault(size = 20, sort = "completedAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    log.debug("Getting completed todos for user: {}", userId);
    Page<TodoView> todos = viewService.getCompletedTodos(userId, pageable);
    return todos.map(mapper::toViewResponse);
  }

  @Operation(summary = "지난 미완료 투두", description = "예정일이 지났지만 완료되지 않은 투두를 조회합니다.")
  @GetMapping("/overdue")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getOverdueTodos(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting overdue todos for user: {}", userId);
    List<TodoView> todos = viewService.getOverdueTodos(userId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "예정된 투두", description = "미래에 예정된 투두를 조회합니다.")
  @GetMapping("/upcoming")
  @PreAuthorize("hasRole('USER')")
  public Page<TodoViewResponse> getUpcomingTodos(
      @AuthenticationPrincipal UUID userId,
      @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    log.debug("Getting upcoming todos for user: {}", userId);
    Page<TodoView> todos = viewService.getUpcomingTodos(userId, pageable);
    return todos.map(mapper::toViewResponse);
  }

  @Operation(summary = "고정된 투두", description = "고정된 투두 목록을 조회합니다.")
  @GetMapping("/pinned")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getPinnedTodos(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting pinned todos for user: {}", userId);
    List<TodoView> todos = viewService.getPinnedTodos(userId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "카테고리별 투두", description = "특정 카테고리의 투두를 조회합니다.")
  @GetMapping("/by-category/{categoryId}")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getTodosByCategory(
      @AuthenticationPrincipal UUID userId,
      @PathVariable UUID categoryId
  ) {
    log.debug("Getting todos for category: {} and user: {}", categoryId, userId);
    List<TodoView> todos = viewService.getTodosByCategory(userId, categoryId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "우선순위별 투두", description = "특정 우선순위의 투두를 조회합니다.")
  @GetMapping("/by-priority/{priorityId}")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getTodosByPriority(
      @AuthenticationPrincipal UUID userId,
      @PathVariable Integer priorityId
  ) {
    log.debug("Getting todos with priority: {} for user: {}", priorityId, userId);
    List<TodoView> todos = viewService.getTodosByPriority(userId, priorityId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "반복 투두", description = "반복 규칙이 있는 투두를 조회합니다.")
  @GetMapping("/recurring")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getRecurringTodos(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting recurring todos for user: {}", userId);
    List<TodoView> todos = viewService.getRecurringTodos(userId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "협업 투두", description = "협업 투두를 조회합니다.")
  @GetMapping("/collaborative")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getCollaborativeTodos(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting collaborative todos for user: {}", userId);
    List<TodoView> todos = viewService.getCollaborativeTodos(userId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "투두 검색", description = "제목이나 설명으로 투두를 검색합니다.")
  @GetMapping("/search")
  @PreAuthorize("hasRole('USER')")
  public Page<TodoViewResponse> searchTodos(
      @AuthenticationPrincipal UUID userId,
      @RequestParam String keyword,
      @PageableDefault(size = 20, sort = "scheduledDate", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    log.debug("Searching todos with keyword: {} for user: {}", keyword, userId);
    Page<TodoView> todos = viewService.searchTodos(userId, keyword, pageable);
    return todos.map(mapper::toViewResponse);
  }

  @Operation(summary = "이번 주 투두", description = "이번 주의 투두를 조회합니다.")
  @GetMapping("/this-week")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getThisWeekTodos(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting this week's todos for user: {}", userId);
    List<TodoView> todos = viewService.getThisWeekTodos(userId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "이번 달 투두", description = "이번 달의 투두를 조회합니다.")
  @GetMapping("/this-month")
  @PreAuthorize("hasRole('USER')")
  public List<TodoViewResponse> getThisMonthTodos(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting this month's todos for user: {}", userId);
    List<TodoView> todos = viewService.getThisMonthTodos(userId);
    return todos.stream()
        .map(mapper::toViewResponse)
        .toList();
  }

  @Operation(summary = "투두 통계", description = "사용자의 투두 통계를 조회합니다.")
  @GetMapping("/statistics")
  @PreAuthorize("hasRole('USER')")
  public TodoViewService.TodoStatistics getTodoStatistics(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting todo statistics for user: {}", userId);
    return viewService.getTodoStatistics(userId);
  }

  @Operation(summary = "날짜별 완료율", description = "날짜별 투두 완료율을 조회합니다.")
  @GetMapping("/statistics/completion-rate")
  @PreAuthorize("hasRole('USER')")
  public List<Map<String, Object>> getCompletionRateByDate(
      @AuthenticationPrincipal UUID userId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    log.debug("Getting completion rate for date range: {} - {} for user: {}", startDate, endDate, userId);
    return viewService.getCompletionRateByDate(userId, startDate, endDate);
  }

  @Operation(summary = "카테고리별 통계", description = "카테고리별 투두 통계를 조회합니다.")
  @GetMapping("/statistics/by-category")
  @PreAuthorize("hasRole('USER')")
  public List<Map<String, Object>> getStatsByCategory(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting stats by category for user: {}", userId);
    return viewService.getStatsByCategory(userId);
  }

  @Operation(summary = "우선순위별 통계", description = "우선순위별 투두 통계를 조회합니다.")
  @GetMapping("/statistics/by-priority")
  @PreAuthorize("hasRole('USER')")
  public List<Map<String, Object>> getStatsByPriority(
      @AuthenticationPrincipal UUID userId
  ) {
    log.debug("Getting stats by priority for user: {}", userId);
    return viewService.getStatsByPriority(userId);
  }
}