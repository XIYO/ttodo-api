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
import point.ttodoApi.user.domain.User;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface TodoDefinitionRepository extends
    JpaRepository<TodoDefinition, UUID>,
    JpaSpecificationExecutor<TodoDefinition> {

  /**
   * 사용자의 활성 정의 목록 조회 (소프트 삭제 제외)
   */
  @Query("""
    SELECT DISTINCT d FROM TodoDefinition d
    LEFT JOIN FETCH d.category
    LEFT JOIN FETCH d.tags
    WHERE d.owner = :owner
      AND d.deletedAt IS NULL
    ORDER BY d.createdAt DESC
    """)
  List<TodoDefinition> findActiveByOwner(@Param("owner") User owner);

  /**
   * 사용자의 활성 정의 페이징 조회
   */
  @Query("""
    SELECT d FROM TodoDefinition d
    WHERE d.owner = :owner
      AND d.deletedAt IS NULL
    """)
  Page<TodoDefinition> findActiveByOwnerPageable(
      @Param("owner") User owner,
      Pageable pageable
  );

  /**
   * 반복 정의만 조회
   */
  @Query("""
    SELECT d FROM TodoDefinition d
    WHERE d.owner = :owner
      AND d.recurrenceRule IS NOT NULL
      AND d.deletedAt IS NULL
    ORDER BY d.createdAt DESC
    """)
  List<TodoDefinition> findRecurringByOwner(@Param("owner") User owner);

  /**
   * 1회용 정의만 조회
   */
  @Query("""
    SELECT d FROM TodoDefinition d
    WHERE d.owner = :owner
      AND d.recurrenceRule IS NULL
      AND d.deletedAt IS NULL
    ORDER BY d.createdAt DESC
    """)
  List<TodoDefinition> findSingleByOwner(@Param("owner") User owner);

  /**
   * 카테고리별 정의 조회
   */
  @Query("""
    SELECT d FROM TodoDefinition d
    WHERE d.category.id = :categoryId
      AND d.deletedAt IS NULL
    ORDER BY d.createdAt DESC
    """)
  List<TodoDefinition> findByCategoryId(@Param("categoryId") UUID categoryId);

  /**
   * 협업 정의 조회
   */
  @Query("""
    SELECT DISTINCT d FROM TodoDefinition d
    LEFT JOIN d.category c
    LEFT JOIN c.collaborators cc
    WHERE (d.owner = :user OR (d.isCollaborative = true AND cc.user = :user))
      AND d.deletedAt IS NULL
    ORDER BY d.createdAt DESC
    """)
  List<TodoDefinition> findCollaborativeByUser(@Param("user") User user);

  /**
   * 소프트 삭제
   */
  @Modifying
  @Query("""
    UPDATE TodoDefinition d
    SET d.deletedAt = :deletedAt
    WHERE d.id = :id
      AND d.deletedAt IS NULL
    """)
  int softDelete(@Param("id") UUID id, @Param("deletedAt") LocalDateTime deletedAt);

  /**
   * 소프트 삭제 복구
   */
  @Modifying
  @Query("""
    UPDATE TodoDefinition d
    SET d.deletedAt = NULL
    WHERE d.id = :id
      AND d.deletedAt IS NOT NULL
    """)
  int restore(@Param("id") UUID id);

  /**
   * 사용자의 모든 정의 소프트 삭제 (계정 삭제 시)
   */
  @Modifying
  @Query("""
    UPDATE TodoDefinition d
    SET d.deletedAt = :deletedAt
    WHERE d.owner = :owner
      AND d.deletedAt IS NULL
    """)
  int softDeleteByOwner(@Param("owner") User owner, @Param("deletedAt") LocalDateTime deletedAt);

  /**
   * 태그로 정의 검색
   */
  @Query("""
    SELECT DISTINCT d FROM TodoDefinition d
    LEFT JOIN d.tags t
    WHERE d.owner = :owner
      AND t IN :tags
      AND d.deletedAt IS NULL
    ORDER BY d.createdAt DESC
    """)
  List<TodoDefinition> findByTags(@Param("owner") User owner, @Param("tags") Set<String> tags);

  /**
   * 제목으로 검색
   */
  @Query("""
    SELECT d FROM TodoDefinition d
    WHERE d.owner = :owner
      AND LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
      AND d.deletedAt IS NULL
    ORDER BY d.createdAt DESC
    """)
  List<TodoDefinition> searchByTitle(@Param("owner") User owner, @Param("keyword") String keyword);

  /**
   * ID와 owner로 조회 (권한 확인용)
   */
  @Query("""
    SELECT d FROM TodoDefinition d
    WHERE d.id = :id
      AND d.owner = :owner
      AND d.deletedAt IS NULL
    """)
  Optional<TodoDefinition> findByIdAndOwner(@Param("id") UUID id, @Param("owner") User owner);

  /**
   * 정의 존재 여부 확인
   */
  @Query("""
    SELECT COUNT(d) > 0 FROM TodoDefinition d
    WHERE d.id = :id
      AND d.deletedAt IS NULL
    """)
  boolean existsByIdAndNotDeleted(@Param("id") UUID id);

  /**
   * 사용자별 통계 조회
   */
  @Query("""
    SELECT
      COUNT(d) as total,
      COUNT(CASE WHEN d.recurrenceRule IS NULL THEN 1 END) as single,
      COUNT(CASE WHEN d.recurrenceRule IS NOT NULL THEN 1 END) as recurring
    FROM TodoDefinition d
    WHERE d.owner = :owner
      AND d.deletedAt IS NULL
    """)
  Map<String, Long> getStatsByOwner(@Param("owner") User owner);
}