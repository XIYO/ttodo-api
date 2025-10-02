package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.todo.domain.*;

import java.util.*;

/**
 * 완료된 Todo(가상 투두가 완료되어 실제로 저장된 것)를 위한 Repository 인터페이스
 */
public interface TodoRepository extends JpaRepository<Todo, TodoId>, JpaSpecificationExecutor<Todo> {

  @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.owner.id = :userId AND t.active = true")
  Optional<Todo> findByTodoIdAndOwnerId(@Param("todoId") TodoId todoId, @Param("userId") UUID userId);

  @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.tags WHERE t.todoId = :todoId AND t.owner.id = :userId AND t.active = true")
  Optional<Todo> findByTodoIdAndOwnerIdWithTags(@Param("todoId") TodoId todoId, @Param("userId") UUID userId);

  @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.owner.id = :userId")
  Optional<Todo> findByTodoIdAndOwnerIdIgnoreActive(@Param("todoId") TodoId todoId, @Param("userId") UUID userId);

  /**
   * 여러 TodoId에 해당하는 Todo를 한 번에 조회 (N+1 방지용 벌크 조회)
   * active 상태 무시하고 조회
   */
  @Query("SELECT t FROM Todo t WHERE t.todoId IN :todoIds AND t.owner.id = :ownerId")
  List<Todo> findAllByTodoIdInAndOwnerIdIgnoreActive(@Param("todoIds") List<TodoId> todoIds, @Param("ownerId") UUID ownerId);

  // 통계용 메서드 - 완료한 할일 수
  @Query("SELECT COUNT(t) FROM Todo t WHERE t.owner.id = :userId AND t.complete = true AND t.active = true")
  long countCompletedTodosByOwnerId(@Param("userId") UUID userId);

  /**
   * 멤버가 접근 가능한 모든 투두 조회 (owner + 협업 투두)
   */
  @Query("SELECT DISTINCT t FROM Todo t " +
          "LEFT JOIN t.category c " +
          "LEFT JOIN c.collaborators cc " +
          "WHERE t.active = true AND " +
          "(" +
          "    t.owner.id = :userId OR " +
          "    (t.isCollaborative = true AND " +
          "     cc.user.id = :userId AND " +
          "     cc.status = 'ACCEPTED' AND " +
          "     cc.deletedAt IS NULL)" +
          ") " +
          "ORDER BY t.createdAt DESC")
        List<Todo> findAccessibleTodosByUserId(@Param("userId") UUID userId);

  /**
   * 특정 카테고리의 협업 투두 조회
   */
  @Query("SELECT t FROM Todo t " +
          "WHERE t.category.id = :categoryId AND " +
          "t.isCollaborative = true AND " +
          "t.active = true " +
          "ORDER BY t.createdAt DESC")
  List<Todo> findCollaborativeTodosByCategoryId(@Param("categoryId") UUID categoryId);

  /**
   * 멤버가 협업자로 참여하는 투두 조회
   */
  @Query("SELECT t FROM Todo t " +
          "JOIN t.category c " +
          "JOIN c.collaborators cc " +
          "WHERE t.isCollaborative = true AND " +
          "t.active = true AND " +
          "cc.user.id = :userId AND " +
          "cc.status = 'ACCEPTED' AND " +
          "cc.deletedAt IS NULL " +
          "ORDER BY t.createdAt DESC")
        List<Todo> findCollaborativeTodosByUserId(@Param("userId") UUID userId);

  /**
   * 특정 TodoId로 멤버가 접근 가능한 투두 조회
   */
  @Query("SELECT t FROM Todo t " +
          "LEFT JOIN t.category c " +
          "LEFT JOIN c.collaborators cc " +
          "WHERE t.todoId = :todoId AND " +
          "t.active = true AND " +
          "(" +
          "    t.owner.id = :userId OR " +
          "    (t.isCollaborative = true AND " +
          "     cc.user.id = :userId AND " +
          "     cc.status = 'ACCEPTED' AND " +
          "     cc.deletedAt IS NULL)" +
          ")")
  Optional<Todo> findAccessibleTodoByTodoIdAndUserId(
          @Param("todoId") TodoId todoId,
          @Param("userId") UUID userId
  );

  /**
   * 카테고리별 협업 투두 수 조회
   */
  @Query("SELECT COUNT(t) FROM Todo t " +
          "WHERE t.category.id = :categoryId AND " +
          "t.isCollaborative = true AND " +
          "t.active = true")
  long countCollaborativeTodosByCategoryId(@Param("categoryId") UUID categoryId);

  /**
   * 멤버의 전체 협업 투두 수 조회
   */
  @Query("SELECT COUNT(DISTINCT t) FROM Todo t " +
          "JOIN t.category c " +
          "JOIN c.collaborators cc " +
          "WHERE t.isCollaborative = true AND " +
          "t.active = true AND " +
          "cc.user.id = :userId AND " +
          "cc.status = 'ACCEPTED' AND " +
          "cc.deletedAt IS NULL")
        long countCollaborativeTodosByUserId(@Param("userId") UUID userId);

  /**
   * 카테고리가 변경되어 협업 범위에서 벗어난 투두들을 일반 투두로 전환
   */
  @Modifying
  @Query("UPDATE Todo t SET t.isCollaborative = false " +
          "WHERE t.category.id = :categoryId AND " +
          "t.isCollaborative = true AND " +
          "NOT EXISTS (" +
          "    SELECT 1 FROM CategoryCollaborator cc " +
          "    WHERE cc.category.id = :categoryId AND " +
          "    cc.status = 'ACCEPTED' AND " +
          "    cc.deletedAt IS NULL" +
          ")")
  int updateCollaborativeTodosToPersonalByCategoryId(@Param("categoryId") UUID categoryId);

  /**
   * 특정 멤버가 협업자에서 제외된 후 해당 멤버의 협업 투두를 개인 투두로 전환
   */
  @Modifying
  @Query("UPDATE Todo t SET t.isCollaborative = false " +
          "WHERE t.owner.id = :userId AND " +
          "t.category.id = :categoryId AND " +
          "t.isCollaborative = true")
  int updateUserCollaborativeTodosToPersonal(
          @Param("userId") UUID userId,
          @Param("categoryId") UUID categoryId
  );

}
