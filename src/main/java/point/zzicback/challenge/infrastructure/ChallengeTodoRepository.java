package point.zzicback.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeTodo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Long> {
    Optional<ChallengeTodo> findByChallengeParticipation(ChallengeParticipation challengeParticipation);
    Optional<ChallengeTodo> findByChallengeParticipationAndTargetDate(ChallengeParticipation challengeParticipation, LocalDate targetDate);
    List<ChallengeTodo> findAllByChallengeParticipation(ChallengeParticipation challengeParticipation);
}
