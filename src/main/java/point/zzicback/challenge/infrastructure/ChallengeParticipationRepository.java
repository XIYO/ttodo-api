package point.zzicback.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.member.domain.Member;

import java.util.List;
import java.util.Optional;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    Optional<ChallengeParticipation> findByMemberAndChallenge_Id(Member member, Long challengeId);
    List<ChallengeParticipation> findByChallenge_Id(Long challengeId);
    boolean existsByMemberAndChallenge_Id(Member member, Long challengeId);

    List<ChallengeParticipation> findByMember(Member member);
}
