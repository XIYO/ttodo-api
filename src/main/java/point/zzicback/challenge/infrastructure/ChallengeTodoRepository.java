package point.zzicback.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeTodo;

import java.util.Optional;

public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Long> {
    Optional<ChallengeTodo> findByChallengeParticipation(ChallengeParticipation challengeParticipation);
}
