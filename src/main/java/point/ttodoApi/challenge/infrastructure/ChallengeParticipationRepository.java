package point.ttodoApi.challenge.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.challenge.domain.ChallengeParticipation;
import point.ttodoApi.user.domain.User;

import java.util.*;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
  // 활성 참여자 조회 (joinOut이 null인 경우)
  List<ChallengeParticipation> findByUserAndJoinOutIsNull(User user);

  Optional<ChallengeParticipation> findByUserAndChallenge_IdAndJoinOutIsNull(User user, Long challengeId);

  boolean existsByUserAndChallenge_IdAndJoinOutIsNull(User user, Long challengeId);

  // 챌린지 ID와 멤버 ID로 참여 여부 확인
  boolean existsByChallenge_IdAndUser_IdAndJoinOutIsNull(Long challengeId, UUID userId);

  // 챌린지 ID와 멤버 ID로 참여 정보 조회
  Optional<ChallengeParticipation> findByChallengeIdAndUserId(Long challengeId, UUID userId);

  // 챌린지의 참여자 목록 조회 (페이지네이션)
  @Query("SELECT cp FROM ChallengeParticipation cp " +
          "JOIN FETCH cp.user " +
          "WHERE cp.challenge.id = :challengeId " +
          "AND cp.joinOut IS NULL")
  Page<ChallengeParticipation> findActiveParticipantsByChallengeId(
          @Param("challengeId") Long challengeId,
          Pageable pageable
  );

  // 특정 멤버의 챌린지 참여 상태 조회
  Optional<ChallengeParticipation> findByChallenge_IdAndUser_IdAndJoinOutIsNull(Long challengeId, UUID userId);

  // 특정 멤버의 모든 참여 현황 조회
  Page<ChallengeParticipation> findByUser_IdAndJoinOutIsNull(UUID userId, Pageable pageable);
}
