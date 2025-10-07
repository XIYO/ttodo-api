package point.ttodoApi.todo.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.shared.exception.ResourceNotFoundException;
import point.ttodoApi.todo.domain.TodoView;
import point.ttodoApi.todo.infrastructure.persistence.TodoViewRepository;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * 투두 통합 조회 서비스 (View 기반)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoViewService {

  private final TodoViewRepository viewRepository;
  private final UserRepository userRepository;

  /**
   * 사용자의 모든 투두 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoView> getUserTodos(UUID userId, Pageable pageable) {
    validateUser(userId);
    return viewRepository.findByUserId(userId, pageable);
  }

  /**
   * 오늘의 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getTodayTodos(UUID userId) {
    validateUser(userId);
    return viewRepository.findTodayTodos(userId, LocalDate.now());
  }

  /**
   * 날짜 범위로 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getTodosByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
    validateUser(userId);

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before or equal to end date");
    }

    return viewRepository.findByDateRange(userId, startDate, endDate);
  }

  /**
   * 미완료 투두 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoView> getPendingTodos(UUID userId, Pageable pageable) {
    validateUser(userId);
    return viewRepository.findPendingTodos(userId, pageable);
  }

  /**
   * 완료된 투두 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoView> getCompletedTodos(UUID userId, Pageable pageable) {
    validateUser(userId);
    return viewRepository.findCompletedTodos(userId, pageable);
  }

  /**
   * 지난 미완료 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getOverdueTodos(UUID userId) {
    validateUser(userId);
    return viewRepository.findOverdueTodos(userId, LocalDate.now());
  }

  /**
   * 예정된 투두 조회
   */
  @Transactional(readOnly = true)
  public Page<TodoView> getUpcomingTodos(UUID userId, Pageable pageable) {
    validateUser(userId);
    return viewRepository.findUpcomingTodos(userId, LocalDate.now(), pageable);
  }

  /**
   * 고정된 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getPinnedTodos(UUID userId) {
    validateUser(userId);
    return viewRepository.findPinnedTodos(userId);
  }

  /**
   * 카테고리별 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getTodosByCategory(UUID userId, UUID categoryId) {
    validateUser(userId);
    return viewRepository.findByCategoryId(userId, categoryId);
  }

  /**
   * 우선순위별 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getTodosByPriority(UUID userId, Integer priorityId) {
    validateUser(userId);

    if (priorityId != null && (priorityId < 0 || priorityId > 2)) {
      throw new IllegalArgumentException("Invalid priority id. Must be 0 (Low), 1 (Medium), or 2 (High)");
    }

    return viewRepository.findByPriorityId(userId, priorityId);
  }

  /**
   * 반복 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getRecurringTodos(UUID userId) {
    validateUser(userId);
    return viewRepository.findRecurringTodos(userId);
  }

  /**
   * 협업 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getCollaborativeTodos(UUID userId) {
    validateUser(userId);
    return viewRepository.findCollaborativeTodos(userId);
  }

  /**
   * 제목으로 투두 검색
   */
  @Transactional(readOnly = true)
  public Page<TodoView> searchTodos(UUID userId, String keyword, Pageable pageable) {
    validateUser(userId);

    if (keyword == null || keyword.trim().isEmpty()) {
      return viewRepository.findByUserId(userId, pageable);
    }

    return viewRepository.searchByTitle(userId, keyword.trim(), pageable);
  }

  /**
   * 투두 통계 조회
   */
  @Transactional(readOnly = true)
  public TodoStatistics getTodoStatistics(UUID userId) {
    validateUser(userId);

    Map<String, Long> stats = viewRepository.getStatsSummary(userId, LocalDate.now());

    return TodoStatistics.builder()
        .total(stats.getOrDefault("total", 0L))
        .completed(stats.getOrDefault("completed", 0L))
        .pending(stats.getOrDefault("pending", 0L))
        .overdue(stats.getOrDefault("overdue", 0L))
        .today(stats.getOrDefault("today", 0L))
        .recurring(stats.getOrDefault("recurring", 0L))
        .completionRate(calculateCompletionRate(stats))
        .build();
  }

  /**
   * 날짜별 완료율 통계
   */
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getCompletionRateByDate(UUID userId, LocalDate startDate, LocalDate endDate) {
    validateUser(userId);

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before or equal to end date");
    }

    return viewRepository.getCompletionRateByDate(userId, startDate, endDate);
  }

  /**
   * 카테고리별 통계
   */
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getStatsByCategory(UUID userId) {
    validateUser(userId);
    return viewRepository.getStatsByCategory(userId);
  }

  /**
   * 우선순위별 통계
   */
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getStatsByPriority(UUID userId) {
    validateUser(userId);
    return viewRepository.getStatsByPriority(userId);
  }

  /**
   * 단일 투두 조회
   */
  @Transactional(readOnly = true)
  public TodoView getTodo(UUID userId, UUID todoId) {
    validateUser(userId);

    TodoView todo = viewRepository.findById(todoId)
        .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + todoId));

    if (!todo.getUserId().equals(userId)) {
      throw new IllegalStateException("User cannot access this todo");
    }

    return todo;
  }

  /**
   * 이번 주 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getThisWeekTodos(UUID userId) {
    validateUser(userId);

    LocalDate today = LocalDate.now();
    LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
    LocalDate endOfWeek = startOfWeek.plusDays(6);

    return viewRepository.findByDateRange(userId, startOfWeek, endOfWeek);
  }

  /**
   * 이번 달 투두 조회
   */
  @Transactional(readOnly = true)
  public List<TodoView> getThisMonthTodos(UUID userId) {
    validateUser(userId);

    LocalDate today = LocalDate.now();
    LocalDate startOfMonth = today.withDayOfMonth(1);
    LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

    return viewRepository.findByDateRange(userId, startOfMonth, endOfMonth);
  }

  /**
   * 사용자 검증
   */
  private void validateUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User not found: " + userId);
    }
  }

  /**
   * 완료율 계산
   */
  private double calculateCompletionRate(Map<String, Long> stats) {
    Long total = stats.getOrDefault("total", 0L);
    Long completed = stats.getOrDefault("completed", 0L);

    if (total == 0) return 0.0;
    return (double) completed / total * 100;
  }

  /**
   * 투두 통계 DTO
   */
  @lombok.Data
  @lombok.Builder
  public static class TodoStatistics {
    private Long total;
    private Long completed;
    private Long pending;
    private Long overdue;
    private Long today;
    private Long recurring;
    private Double completionRate;
  }
}