package point.zzicback.challenge.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeVisibility;
import point.zzicback.challenge.domain.PeriodType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    @Query(value = "SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.participations p LEFT JOIN FETCH p.member",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Challenge c")
    Page<Challenge> findAllWithParticipations(Pageable pageable);

    @Query("SELECT c FROM Challenge c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Challenge> searchByKeyword(String keyword, Pageable pageable);

    @Query("SELECT c FROM Challenge c LEFT JOIN c.participations p " +
           "GROUP BY c " +
           "ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC, c.startDate DESC")
    Page<Challenge> findAllOrderedByPopularity(Pageable pageable);

    @Query("SELECT c FROM Challenge c LEFT JOIN c.participations p " +
           "WHERE (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "GROUP BY c " +
           "ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC, c.startDate DESC")
    Page<Challenge> searchByKeywordOrderedByPopularity(String keyword, Pageable pageable);
    
    // 공개 챌린지만 조회
    @Query("SELECT c FROM Challenge c WHERE c.visibility = :visibility " +
           "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:periodType IS NULL OR c.periodType = :periodType)")
    Page<Challenge> findByVisibilityAndFilters(
        @Param("visibility") ChallengeVisibility visibility,
        @Param("keyword") String keyword,
        @Param("periodType") PeriodType periodType,
        Pageable pageable
    );
    
    // 공개 챌린지 인기순 정렬
    @Query("SELECT c FROM Challenge c LEFT JOIN c.participations p " +
           "WHERE c.visibility = :visibility " +
           "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "GROUP BY c " +
           "ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC")
    Page<Challenge> findByVisibilityOrderByParticipantCount(
        @Param("visibility") ChallengeVisibility visibility,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    // 초대 코드로 조회
    Optional<Challenge> findByInviteCode(String inviteCode);
    
    // 활성 챌린지 조회 (정책 실행용)
    @Query("SELECT c FROM Challenge c WHERE c.startDate <= :today AND c.endDate >= :today")
    List<Challenge> findActiveChallenges(@Param("today") LocalDate today);
}
