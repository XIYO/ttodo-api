package point.zzicback.challenge.infrastructure;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.zzicback.challenge.domain.*;

import java.time.LocalDate;
import java.util.Optional;

public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Long> {

    Optional<ChallengeTodo> findByChallengeParticipation(ChallengeParticipation challengeParticipation);
    Optional<ChallengeTodo> findByChallengeParticipationAndTargetDate(ChallengeParticipation challengeParticipation, LocalDate targetDate);
    @Query("SELECT COUNT(DISTINCT ct.challengeParticipation.id) FROM ChallengeTodo ct WHERE ct.challengeParticipation.challenge.id = :challengeId AND ct.done = true")
    long countCompletedParticipantsByChallengeId(@Param("challengeId") Long challengeId);
}
