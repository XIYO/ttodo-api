package point.zzicback.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.member.domain.Member;

import java.util.*;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    // 활성 참여자 조회 (joinOut이 null인 경우)
    List<ChallengeParticipation> findByMemberAndJoinOutIsNull(Member member);
    Optional<ChallengeParticipation> findByMemberAndChallenge_IdAndJoinOutIsNull(Member member, Long challengeId);
    boolean existsByMemberAndChallenge_IdAndJoinOutIsNull(Member member, Long challengeId);
}
