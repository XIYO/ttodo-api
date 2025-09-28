package point.ttodoApi.category.infrastructure.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import point.ttodoApi.category.domain.*;
import point.ttodoApi.user.domain.User;

import java.util.*;

/**
 * CategoryCollaborator Repository
 */
@Repository
public interface CategoryCollaboratorRepository extends JpaRepository<CategoryCollaborator, Long> {

  /**
   * 특정 카테고리와 멤버로 협업자 조회
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "WHERE cc.category = :category AND cc.user = :user " +
          "AND cc.deletedAt IS NULL")
  Optional<CategoryCollaborator> findByCategoryAndUser(
          @Param("category") Category category,
          @Param("user") User user
  );

  /**
   * 멤버와 상태로 협업자 목록 조회
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "WHERE cc.user = :user AND cc.status = :status " +
          "AND cc.deletedAt IS NULL " +
          "ORDER BY cc.invitedAt DESC")
  List<CategoryCollaborator> findByUserAndStatus(
          @Param("user") User user,
          @Param("status") CollaboratorStatus status
  );

  /**
   * 멤버의 모든 협업 관계 조회 (상태 무관)
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "WHERE cc.user = :user AND cc.deletedAt IS NULL " +
          "ORDER BY cc.invitedAt DESC")
  List<CategoryCollaborator> findByUser(@Param("user") User user);

  /**
   * 카테고리의 모든 협업자 조회
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "WHERE cc.category = :category AND cc.deletedAt IS NULL " +
          "ORDER BY cc.acceptedAt DESC, cc.invitedAt DESC")
  List<CategoryCollaborator> findByCategory(@Param("category") Category category);

  /**
   * 카테고리의 활성 협업자 조회 (수락된 협업자만)
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "WHERE cc.category = :category AND cc.status = 'ACCEPTED' " +
          "AND cc.deletedAt IS NULL " +
          "ORDER BY cc.acceptedAt ASC")
  List<CategoryCollaborator> findActiveByCategoryId(@Param("category") Category category);

  /**
   * 카테고리의 수락된 협업자 수 조회
   */
  @Query("SELECT COUNT(cc) FROM CategoryCollaborator cc " +
          "WHERE cc.category = :category AND cc.status = 'ACCEPTED' " +
          "AND cc.deletedAt IS NULL")
  long countAcceptedCollaboratorsByCategory(@Param("category") Category category);

  /**
   * 특정 멤버와 카테고리, 상태로 존재 여부 확인
   */
  @Query("SELECT COUNT(cc) > 0 FROM CategoryCollaborator cc " +
          "WHERE cc.category = :category AND cc.user = :user " +
          "AND cc.status = :status AND cc.deletedAt IS NULL")
  boolean existsByCategoryAndUserAndStatus(
          @Param("category") Category category,
          @Param("user") User user,
          @Param("status") CollaboratorStatus status
  );

  /**
   * 멤버가 협업자인 카테고리 ID 목록 조회
   */
  @Query("SELECT cc.category.id FROM CategoryCollaborator cc " +
          "WHERE cc.user = :user AND cc.status = 'ACCEPTED' " +
          "AND cc.deletedAt IS NULL")
  List<UUID> findCollaborativeCategoryIdsByUser(@Param("user") User user);

  /**
   * 멤버가 협업자인 카테고리 목록 조회
   */
  @Query("SELECT cc.category FROM CategoryCollaborator cc " +
          "WHERE cc.user = :user AND cc.status = 'ACCEPTED' " +
          "AND cc.deletedAt IS NULL " +
          "ORDER BY cc.acceptedAt DESC")
  List<Category> findCollaborativeCategoriesByUser(@Param("user") User user);

  /**
   * 특정 카테고리의 대기 중인 초대 목록 조회
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "WHERE cc.category = :category AND cc.status = 'PENDING' " +
          "AND cc.deletedAt IS NULL " +
          "ORDER BY cc.invitedAt DESC")
  List<CategoryCollaborator> findPendingInvitationsByCategory(@Param("category") Category category);

  /**
   * 멤버의 대기 중인 초대 목록 조회
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "WHERE cc.user = :user AND cc.status = 'PENDING' " +
          "AND cc.deletedAt IS NULL " +
          "ORDER BY cc.invitedAt DESC")
  List<CategoryCollaborator> findPendingInvitationsByUser(@Param("user") User user);

  /**
   * 특정 owner가 소유한 카테고리들의 모든 협업자 조회
   */
  @Query("SELECT cc FROM CategoryCollaborator cc " +
          "JOIN cc.category c " +
          "WHERE c.owner = :owner AND cc.deletedAt IS NULL " +
          "ORDER BY c.name, cc.acceptedAt DESC, cc.invitedAt DESC")
  List<CategoryCollaborator> findByCategoryOwner(@Param("owner") User owner);

  /**
   * 중복 초대 방지를 위한 존재 여부 확인
   */
  @Query("SELECT COUNT(cc) > 0 FROM CategoryCollaborator cc " +
          "WHERE cc.category = :category AND cc.user = :user " +
          "AND cc.status IN ('PENDING', 'ACCEPTED') " +
          "AND cc.deletedAt IS NULL")
  boolean existsActiveInvitation(
          @Param("category") Category category,
          @Param("user") User user
  );
}