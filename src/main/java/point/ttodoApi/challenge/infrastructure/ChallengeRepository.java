package point.ttodoApi.challenge.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.challenge.domain.*;

import java.time.LocalDate;
import java.util.*;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, JpaSpecificationExecutor<Challenge> {

  @Query(value = "SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.participations p LEFT JOIN FETCH p.user",
          countQuery = "SELECT COUNT(DISTINCT c) FROM Challenge c")
  Page<Challenge> findAllWithParticipations(Pageable pageable);

  @Query("""
      SELECT c FROM Challenge c 
      WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
      OR c.description LIKE CONCAT('%', :keyword, '%')
      """)
  Page<Challenge> searchByKeyword(String keyword, Pageable pageable);

  @Query("""
      SELECT c FROM Challenge c 
      LEFT JOIN c.participations p
      GROUP BY c
      ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC, c.startDate DESC
      """)
  Page<Challenge> findAllOrderedByPopularity(Pageable pageable);

  @Query("""
      SELECT c FROM Challenge c 
      LEFT JOIN c.participations p
      WHERE (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR c.description LIKE CONCAT('%', :keyword, '%'))
      GROUP BY c
      ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC, c.startDate DESC
      """)
  Page<Challenge> searchByKeywordOrderedByPopularity(String keyword, Pageable pageable);

  // 공개 챌린지만 조회
  @Query("""
      SELECT c FROM Challenge c 
      WHERE c.visibility = :visibility
      AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR c.description LIKE CONCAT('%', :keyword, '%'))
      AND (:periodType IS NULL OR c.periodType = :periodType)
      """)
  Page<Challenge> findByVisibilityAndFilters(
          @Param("visibility") ChallengeVisibility visibility,
          @Param("keyword") String keyword,
          @Param("periodType") PeriodType periodType,
          Pageable pageable
  );

  // 공개 챌린지 인기순 정렬
  @Query("""
      SELECT c FROM Challenge c 
      LEFT JOIN c.participations p
      WHERE c.visibility = :visibility
      AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR c.description LIKE CONCAT('%', :keyword, '%'))
      GROUP BY c
      ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC
      """)
  Page<Challenge> findByVisibilityOrderByParticipantCount(
          @Param("visibility") ChallengeVisibility visibility,
          @Param("keyword") String keyword,
          Pageable pageable
  );

  // 초대 코드로 조회
  Optional<Challenge> findByInviteCode(String inviteCode);

  // 활성 챌린지 조회 (정책 실행용)
  @Query("""
      SELECT c FROM Challenge c 
      WHERE c.startDate <= :today 
      AND c.endDate >= :today
      """)
  List<Challenge> findActiveChallenges(@Param("today") LocalDate today);

  // 다이나믹 쿼리
  @Query("""
      SELECT c FROM Challenge c 
      WHERE c.active = true
      AND (:titleKeyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :titleKeyword, '%')))
      AND (:descriptionKeyword IS NULL OR c.description LIKE CONCAT('%', :descriptionKeyword, '%'))
      AND (:visibility IS NULL OR c.visibility = :visibility)
      AND (:periodType IS NULL OR c.periodType = :periodType)
      AND (:creatorId IS NULL OR c.creatorId = :creatorId)
      AND (:ongoingOnly = false OR (c.startDate <= CURRENT_DATE AND c.endDate >= CURRENT_DATE))
      AND (:joinableOnly = false OR c.maxParticipants IS NULL)
      """)
  Page<Challenge> findChallengesWithDynamicQuery(
          @Param("titleKeyword") String titleKeyword,
          @Param("descriptionKeyword") String descriptionKeyword,
          @Param("visibility") ChallengeVisibility visibility,
          @Param("periodType") PeriodType periodType,
          @Param("creatorId") UUID creatorId,
          @Param("ongoingOnly") boolean ongoingOnly,
          @Param("joinableOnly") boolean joinableOnly,
          Pageable pageable
  );

  // 내가 생성한 활성 챌린지
  List<Challenge> findByCreatorIdAndActiveTrue(UUID creatorId);

  // 공개되고 진행 중인 챌린지
  @Query("""
      SELECT c FROM Challenge c 
      WHERE c.active = true
      AND c.visibility = 'PUBLIC'
      AND c.startDate <= :now
      AND c.endDate >= :now
      """)
  Page<Challenge> findPublicOngoingChallenges(@Param("now") LocalDate now, Pageable pageable);
}
