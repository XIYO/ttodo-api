package point.zzicback.challenge.infrastructure;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.zzicback.challenge.domain.*;

import java.time.LocalDate;
import java.util.*;

public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Long> {
    Optional<ChallengeTodo> findByChallengeParticipation(ChallengeParticipation challengeParticipation);
    Optional<ChallengeTodo> findByChallengeParticipationAndTargetDate(ChallengeParticipation challengeParticipation, LocalDate targetDate);
    List<ChallengeTodo> findAllByChallengeParticipation(ChallengeParticipation challengeParticipation);
    List<ChallengeTodo> findAllByChallengeParticipationAndDoneTrue(ChallengeParticipation challengeParticipation);
    
    @Query("SELECT COUNT(DISTINCT ct.challengeParticipation.id) FROM ChallengeTodo ct WHERE ct.challengeParticipation.challenge.id = :challengeId AND ct.done = true")
    long countCompletedParticipantsByChallengeId(@Param("challengeId") Long challengeId);
}
