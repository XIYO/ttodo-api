package point.ttodoApi.challenge.infrastructure;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.user.domain.User;

import java.util.*;

/**
 * ChallengeLeader Repository
 */
@Repository
public interface ChallengeLeaderRepository extends JpaRepository<ChallengeLeader, Long> {

  /**
   * 특정 챌린지의 활성 리더 목록 조회
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge AND cl.status = 'ACTIVE' " +
          "ORDER BY cl.appointedAt ASC")
  List<ChallengeLeader> findActiveLeadersByChallenge(@Param("challenge") Challenge challenge);

  /**
   * 특정 챌린지와 멤버로 리더 조회 (상태 무관)
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge AND cl.user = :user")
  Optional<ChallengeLeader> findByChallengeAndUser(
          @Param("challenge") Challenge challenge,
          @Param("user") User user
  );

  /**
   * 특정 챌린지와 멤버로 활성 리더 조회
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge AND cl.user = :user " +
          "AND cl.status = 'ACTIVE'")
  Optional<ChallengeLeader> findActiveLeaderByChallengeAndUser(
          @Param("challenge") Challenge challenge,
          @Param("user") User user
  );

  /**
   * 멤버가 리더인 모든 챌린지 조회
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.user = :user AND cl.status = 'ACTIVE' " +
          "ORDER BY cl.appointedAt DESC")
  List<ChallengeLeader> findActiveChallengesByUser(@Param("user") User user);

  /**
   * 멤버가 리더인 챌린지 수 조회
   */
  @Query("SELECT COUNT(cl) FROM ChallengeLeader cl " +
          "WHERE cl.user = :user AND cl.status = 'ACTIVE'")
  long countActiveChallengesByUser(@Param("user") User user);

  /**
   * 챌린지의 활성 리더 수 조회
   */
  @Query("SELECT COUNT(cl) FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge AND cl.status = 'ACTIVE'")
  long countActiveLeadersByChallenge(@Param("challenge") Challenge challenge);

  /**
   * 특정 멤버가 특정 챌린지의 활성 리더인지 확인
   */
  @Query("SELECT COUNT(cl) > 0 FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge AND cl.user = :user " +
          "AND cl.status = 'ACTIVE'")
  boolean existsActiveLeaderByChallengeAndUser(
          @Param("challenge") Challenge challenge,
          @Param("user") User user
  );

  /**
   * 챌린지 ID로 활성 리더 목록 조회
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.challenge.id = :challengeId AND cl.status = 'ACTIVE' " +
          "ORDER BY cl.appointedAt ASC")
  List<ChallengeLeader> findActiveLeadersByChallengeId(@Param("challengeId") Long challengeId);

  /**
   * 멤버 ID로 리더인 챌린지 목록 조회
   */
  @Query("SELECT cl.challenge FROM ChallengeLeader cl " +
          "WHERE cl.user.id = :userId AND cl.status = 'ACTIVE' " +
          "ORDER BY cl.appointedAt DESC")
        List<Challenge> findActiveChallengesByUserId(@Param("userId") UUID userId);

  /**
   * 특정 챌린지의 모든 리더 기록 조회 (제거된 리더 포함)
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge " +
          "ORDER BY cl.appointedAt DESC")
  List<ChallengeLeader> findAllLeadersByChallengeOrderByAppointed(@Param("challenge") Challenge challenge);

  /**
   * 특정 기간 내에 임명된 리더 목록 조회
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge AND cl.status = 'ACTIVE' " +
          "AND cl.appointedAt >= :fromDate " +
          "ORDER BY cl.appointedAt DESC")
  List<ChallengeLeader> findActiveLeadersAppointedAfter(
          @Param("challenge") Challenge challenge,
          @Param("fromDate") java.time.LocalDateTime fromDate
  );

  /**
   * 특정 사용자가 임명한 리더 목록 조회
   */
  @Query("SELECT cl FROM ChallengeLeader cl " +
          "WHERE cl.challenge = :challenge AND cl.appointedBy = :appointedBy " +
          "ORDER BY cl.appointedAt DESC")
  List<ChallengeLeader> findLeadersAppointedBy(
          @Param("challenge") Challenge challenge,
          @Param("appointedBy") UUID appointedBy
  );

  /**
   * 활성 리더가 있는 챌린지 ID 목록 조회
   */
  @Query("SELECT DISTINCT cl.challenge.id FROM ChallengeLeader cl " +
          "WHERE cl.status = 'ACTIVE'")
  List<Long> findChallengeIdsWithActiveLeaders();

  /**
   * 멤버가 활성 리더인 챌린지 ID 목록 조회
   */
  @Query("SELECT cl.challenge.id FROM ChallengeLeader cl " +
          "WHERE cl.user.id = :userId AND cl.status = 'ACTIVE'")
  List<Long> findChallengeIdsByActiveLeaderuserId(@Param("userId") UUID userId);
}