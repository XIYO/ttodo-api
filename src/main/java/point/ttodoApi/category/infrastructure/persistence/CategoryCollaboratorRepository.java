package point.ttodoApi.category.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.domain.CategoryCollaborator;
import point.ttodoApi.category.domain.CollaboratorStatus;
import point.ttodoApi.member.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CategoryCollaborator Repository
 */
@Repository
public interface CategoryCollaboratorRepository extends JpaRepository<CategoryCollaborator, Long> {
    
    /**
     * 특정 카테고리와 멤버로 협업자 조회
     */
    @Query("SELECT cc FROM CategoryCollaborator cc " +
           "WHERE cc.category = :category AND cc.member = :member " +
           "AND cc.deletedAt IS NULL")
    Optional<CategoryCollaborator> findByCategoryAndMember(
        @Param("category") Category category, 
        @Param("member") Member member
    );
    
    /**
     * 멤버와 상태로 협업자 목록 조회
     */
    @Query("SELECT cc FROM CategoryCollaborator cc " +
           "WHERE cc.member = :member AND cc.status = :status " +
           "AND cc.deletedAt IS NULL " +
           "ORDER BY cc.invitedAt DESC")
    List<CategoryCollaborator> findByMemberAndStatus(
        @Param("member") Member member, 
        @Param("status") CollaboratorStatus status
    );
    
    /**
     * 멤버의 모든 협업 관계 조회 (상태 무관)
     */
    @Query("SELECT cc FROM CategoryCollaborator cc " +
           "WHERE cc.member = :member AND cc.deletedAt IS NULL " +
           "ORDER BY cc.invitedAt DESC")
    List<CategoryCollaborator> findByMember(@Param("member") Member member);
    
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
           "WHERE cc.category = :category AND cc.member = :member " +
           "AND cc.status = :status AND cc.deletedAt IS NULL")
    boolean existsByCategoryAndMemberAndStatus(
        @Param("category") Category category, 
        @Param("member") Member member, 
        @Param("status") CollaboratorStatus status
    );
    
    /**
     * 멤버가 협업자인 카테고리 ID 목록 조회
     */
    @Query("SELECT cc.category.id FROM CategoryCollaborator cc " +
           "WHERE cc.member = :member AND cc.status = 'ACCEPTED' " +
           "AND cc.deletedAt IS NULL")
    List<UUID> findCollaborativeCategoryIdsByMember(@Param("member") Member member);
    
    /**
     * 멤버가 협업자인 카테고리 목록 조회
     */
    @Query("SELECT cc.category FROM CategoryCollaborator cc " +
           "WHERE cc.member = :member AND cc.status = 'ACCEPTED' " +
           "AND cc.deletedAt IS NULL " +
           "ORDER BY cc.acceptedAt DESC")
    List<Category> findCollaborativeCategoriesByMember(@Param("member") Member member);
    
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
           "WHERE cc.member = :member AND cc.status = 'PENDING' " +
           "AND cc.deletedAt IS NULL " +
           "ORDER BY cc.invitedAt DESC")
    List<CategoryCollaborator> findPendingInvitationsByMember(@Param("member") Member member);
    
    /**
     * 특정 owner가 소유한 카테고리들의 모든 협업자 조회
     */
    @Query("SELECT cc FROM CategoryCollaborator cc " +
           "JOIN cc.category c " +
           "WHERE c.owner = :owner AND cc.deletedAt IS NULL " +
           "ORDER BY c.name, cc.acceptedAt DESC, cc.invitedAt DESC")
    List<CategoryCollaborator> findByCategoryOwner(@Param("owner") Member owner);
    
    /**
     * 중복 초대 방지를 위한 존재 여부 확인
     */
    @Query("SELECT COUNT(cc) > 0 FROM CategoryCollaborator cc " +
           "WHERE cc.category = :category AND cc.member = :member " +
           "AND cc.status IN ('PENDING', 'ACCEPTED') " +
           "AND cc.deletedAt IS NULL")
    boolean existsActiveInvitation(
        @Param("category") Category category, 
        @Param("member") Member member
    );
}