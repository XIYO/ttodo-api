package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import point.ttodoApi.todo.domain.TodoView;

import java.time.LocalDate;
import java.util.*;

/**
 * 투두 통합 뷰 Repository (읽기 전용)
 */
@Repository
public interface TodoViewRepository extends
    JpaRepository<TodoView, UUID>,
    JpaSpecificationExecutor<TodoView> {

  /**
   * 사용자의 모든 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
    ORDER BY v.isPinned DESC, v.dueDate ASC, v.dueTime ASC
    """)
  Page<TodoView> findByUserId(@Param("userId") UUID userId, Pageable pageable);

  /**
   * 오늘의 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.dueDate = :today
    ORDER BY v.completed ASC, v.isPinned DESC, v.displayOrder ASC, v.dueTime ASC
    """)
  List<TodoView> findTodayTodos(
      @Param("userId") UUID userId,
      @Param("today") LocalDate today
  );

  /**
   * 날짜 범위로 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.dueDate BETWEEN :startDate AND :endDate
    ORDER BY v.dueDate ASC, v.dueTime ASC
    """)
  List<TodoView> findByDateRange(
      @Param("userId") UUID userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * 미완료 투두만 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.completed != true
    ORDER BY v.dueDate ASC, v.dueTime ASC
    """)
  Page<TodoView> findPendingTodos(@Param("userId") UUID userId, Pageable pageable);

  /**
   * 완료된 투두만 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.completed = true
    ORDER BY v.completedAt DESC
    """)
  Page<TodoView> findCompletedTodos(@Param("userId") UUID userId, Pageable pageable);

  /**
   * 지난 미완료 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.dueDate < :today
      AND v.completed != true
    ORDER BY v.dueDate DESC
    """)
  List<TodoView> findOverdueTodos(
      @Param("userId") UUID userId,
      @Param("today") LocalDate today
  );

  /**
   * 예정된 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.dueDate > :today
      AND v.completed != true
    ORDER BY v.dueDate ASC, v.dueTime ASC
    """)
  Page<TodoView> findUpcomingTodos(
      @Param("userId") UUID userId,
      @Param("today") LocalDate today,
      Pageable pageable
  );

  /**
   * 고정된 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.isPinned = true
    ORDER BY v.displayOrder ASC, v.dueDate ASC
    """)
  List<TodoView> findPinnedTodos(@Param("userId") UUID userId);

  /**
   * 카테고리별 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.categoryId = :categoryId
    ORDER BY v.dueDate ASC, v.dueTime ASC
    """)
  List<TodoView> findByCategoryId(
      @Param("userId") UUID userId,
      @Param("categoryId") UUID categoryId
  );

  /**
   * 우선순위별 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.priorityId = :priorityId
    ORDER BY v.dueDate ASC, v.dueTime ASC
    """)
  List<TodoView> findByPriorityId(
      @Param("userId") UUID userId,
      @Param("priorityId") Integer priorityId
  );

  /**
   * 반복 투두만 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.isRecurring = true
    ORDER BY v.dueDate ASC, v.dueTime ASC
    """)
  List<TodoView> findRecurringTodos(@Param("userId") UUID userId);

  /**
   * 협업 투두 조회
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND v.isCollaborative = true
    ORDER BY v.dueDate ASC, v.dueTime ASC
    """)
  List<TodoView> findCollaborativeTodos(@Param("userId") UUID userId);

  /**
   * 제목으로 검색
   */
  @Query("""
    SELECT v FROM TodoView v
    WHERE v.userId = :userId
      AND LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY v.dueDate ASC
    """)
  Page<TodoView> searchByTitle(
      @Param("userId") UUID userId,
      @Param("keyword") String keyword,
      Pageable pageable
  );

  /**
   * 통계 조회 - 전체 요약
   */
  @Query("""
    SELECT
      COUNT(v) as total,
      COUNT(CASE WHEN v.completed = true THEN 1 END) as completed,
      COUNT(CASE WHEN v.completed != true THEN 1 END) as pending,
      COUNT(CASE WHEN v.dueDate < :today AND v.completed != true THEN 1 END) as overdue,
      COUNT(CASE WHEN v.dueDate = :today THEN 1 END) as today,
      COUNT(CASE WHEN v.isRecurring = true THEN 1 END) as recurring
    FROM TodoView v
    WHERE v.userId = :userId
    """)
  Map<String, Long> getStatsSummary(
      @Param("userId") UUID userId,
      @Param("today") LocalDate today
  );

  /**
   * 날짜별 완료율 통계
   */
  @Query("""
    SELECT
      v.dueDate as date,
      COUNT(CASE WHEN v.completed = true THEN 1 END) as completed,
      COUNT(CASE WHEN v.completed != true THEN 1 END) as pending
    FROM TodoView v
    WHERE v.userId = :userId
      AND v.dueDate BETWEEN :startDate AND :endDate
    GROUP BY v.dueDate
    ORDER BY v.dueDate ASC
    """)
  List<Map<String, Object>> getCompletionRateByDate(
      @Param("userId") UUID userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * 카테고리별 통계
   */
  @Query("""
    SELECT
      v.categoryId as categoryId,
      COUNT(v) as total,
      COUNT(CASE WHEN v.completed = true THEN 1 END) as completed
    FROM TodoView v
    WHERE v.userId = :userId
    GROUP BY v.categoryId
    """)
  List<Map<String, Object>> getStatsByCategory(@Param("userId") UUID userId);

  /**
   * 우선순위별 통계
   */
  @Query("""
    SELECT
      v.priorityId as priorityId,
      COUNT(v) as total,
      COUNT(CASE WHEN v.completed = true THEN 1 END) as completed
    FROM TodoView v
    WHERE v.userId = :userId
    GROUP BY v.priorityId
    ORDER BY v.priorityId DESC NULLS LAST
    """)
  List<Map<String, Object>> getStatsByPriority(@Param("userId") UUID userId);
}