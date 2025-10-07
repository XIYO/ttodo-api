package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import point.ttodoApi.todo.domain.TodoDefinition;
import point.ttodoApi.todo.domain.TodoInstance;
import point.ttodoApi.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface TodoInstanceRepository extends
    JpaRepository<TodoInstance, UUID>,
    JpaSpecificationExecutor<TodoInstance> {

  /**
   * 날짜 범위로 인스턴스 조회
   */
  @Query("""
    SELECT DISTINCT i FROM TodoInstance i
    LEFT JOIN FETCH i.definition d
    LEFT JOIN FETCH i.category
    WHERE i.owner = :owner
      AND i.dueDate BETWEEN :startDate AND :endDate
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    ORDER BY i.dueDate ASC, i.dueTime ASC
    """)
  List<TodoInstance> findByDateRange(
      @Param("owner") User owner,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * 오늘의 인스턴스 조회
   */
  @Query("""
    SELECT DISTINCT i FROM TodoInstance i
    LEFT JOIN FETCH i.definition d
    LEFT JOIN FETCH i.category
    WHERE i.owner = :owner
      AND i.dueDate = :today
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    ORDER BY i.completed ASC, i.isPinned DESC, i.displayOrder ASC, i.dueTime ASC
    """)
  List<TodoInstance> findTodayInstances(
      @Param("owner") User owner,
      @Param("today") LocalDate today
  );

  /**
   * 지난 미완료 인스턴스 조회
   */
  @Query("""
    SELECT i FROM TodoInstance i
    INNER JOIN i.definition d
    WHERE i.owner = :owner
      AND i.dueDate < :today
      AND i.completed = false
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    ORDER BY i.dueDate DESC
    """)
  List<TodoInstance> findOverdueInstances(
      @Param("owner") User owner,
      @Param("today") LocalDate today
  );

  /**
   * 예정된 인스턴스 조회
   */
  @Query("""
    SELECT i FROM TodoInstance i
    INNER JOIN i.definition d
    WHERE i.owner = :owner
      AND i.dueDate > :today
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    ORDER BY i.dueDate ASC, i.dueTime ASC
    """)
  Page<TodoInstance> findUpcomingInstances(
      @Param("owner") User owner,
      @Param("today") LocalDate today,
      Pageable pageable
  );

  /**
   * 정의별 인스턴스 조회
   */
  @Query("""
    SELECT i FROM TodoInstance i
    WHERE i.definition = :definition
      AND i.deletedAt IS NULL
    ORDER BY i.sequenceNumber ASC
    """)
  List<TodoInstance> findByDefinition(@Param("definition") TodoDefinition definition);

  /**
   * 완료된 인스턴스 조회
   */
  @Query("""
    SELECT i FROM TodoInstance i
    INNER JOIN i.definition d
    WHERE i.owner = :owner
      AND i.completed = true
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    ORDER BY i.completedAt DESC
    """)
  Page<TodoInstance> findCompletedInstances(
      @Param("owner") User owner,
      Pageable pageable
  );

  /**
   * 고정된 인스턴스 조회
   */
  @Query("""
    SELECT i FROM TodoInstance i
    INNER JOIN i.definition d
    WHERE i.owner = :owner
      AND i.isPinned = true
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    ORDER BY i.displayOrder ASC, i.dueDate ASC
    """)
  List<TodoInstance> findPinnedInstances(@Param("owner") User owner);

  /**
   * 인스턴스 완료 처리
   */
  @Modifying
  @Query("""
    UPDATE TodoInstance i
    SET i.completed = true, i.completedAt = :completedAt
    WHERE i.id = :id
      AND i.completed = false
      AND i.deletedAt IS NULL
    """)
  int markComplete(@Param("id") UUID id, @Param("completedAt") LocalDateTime completedAt);

  /**
   * 인스턴스 완료 취소
   */
  @Modifying
  @Query("""
    UPDATE TodoInstance i
    SET i.completed = false, i.completedAt = null
    WHERE i.id = :id
      AND i.completed = true
      AND i.deletedAt IS NULL
    """)
  int markIncomplete(@Param("id") UUID id);

  /**
   * 소프트 삭제
   */
  @Modifying
  @Query("""
    UPDATE TodoInstance i
    SET i.deletedAt = :deletedAt
    WHERE i.id = :id
      AND i.deletedAt IS NULL
    """)
  int softDelete(@Param("id") UUID id, @Param("deletedAt") LocalDateTime deletedAt);

  /**
   * 정의별 모든 인스턴스 소프트 삭제
   */
  @Modifying
  @Query("""
    UPDATE TodoInstance i
    SET i.deletedAt = :deletedAt
    WHERE i.definition = :definition
      AND i.deletedAt IS NULL
    """)
  int softDeleteByDefinition(
      @Param("definition") TodoDefinition definition,
      @Param("deletedAt") LocalDateTime deletedAt
  );

  /**
   * 카테고리별 인스턴스 조회
   */
  @Query("""
    SELECT i FROM TodoInstance i
    INNER JOIN i.definition d
    WHERE i.owner = :owner
      AND (i.category.id = :categoryId OR d.category.id = :categoryId)
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    ORDER BY i.dueDate ASC
    """)
  List<TodoInstance> findByCategoryId(
      @Param("owner") User owner,
      @Param("categoryId") UUID categoryId
  );

  /**
   * 통계 조회 - 날짜별 완료 수
   */
  @Query("""
    SELECT
      i.dueDate as date,
      COUNT(CASE WHEN i.completed = true THEN 1 END) as completed,
      COUNT(CASE WHEN i.completed = false THEN 1 END) as pending
    FROM TodoInstance i
    INNER JOIN i.definition d
    WHERE i.owner = :owner
      AND i.dueDate BETWEEN :startDate AND :endDate
      AND i.deletedAt IS NULL
      AND d.deletedAt IS NULL
    GROUP BY i.dueDate
    ORDER BY i.dueDate ASC
    """)
  List<Map<String, Object>> getCompletionStatsByDateRange(
      @Param("owner") User owner,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * ID와 owner로 조회 (권한 확인용)
   */
  @Query("""
    SELECT i FROM TodoInstance i
    WHERE i.id = :id
      AND i.owner = :owner
      AND i.deletedAt IS NULL
    """)
  Optional<TodoInstance> findByIdAndOwner(@Param("id") UUID id, @Param("owner") User owner);

  /**
   * 다음 시퀀스 번호 조회
   */
  @Query("""
    SELECT COALESCE(MAX(i.sequenceNumber), 0) + 1
    FROM TodoInstance i
    WHERE i.definition = :definition
    """)
  Integer getNextSequenceNumber(@Param("definition") TodoDefinition definition);

  /**
   * 특정 시퀀스의 인스턴스 조회
   */
  @Query("""
    SELECT i FROM TodoInstance i
    WHERE i.definition = :definition
      AND i.sequenceNumber = :sequenceNumber
      AND i.deletedAt IS NULL
    """)
  Optional<TodoInstance> findByDefinitionAndSequence(
      @Param("definition") TodoDefinition definition,
      @Param("sequenceNumber") Integer sequenceNumber
  );
}