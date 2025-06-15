package point.zzicback.challenge.application.dto.result;

import point.zzicback.challenge.domain.PeriodType;
import java.time.LocalDate;

public record ChallengeListResult(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PeriodType periodType,
        Boolean participationStatus,
        Integer activeParticipantCount
) {
}
