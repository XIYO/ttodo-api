package point.zzicback.challenge.application.dto.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.domain.ChallengeVisibility;

import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChallengeResult(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PeriodType periodType,
        Boolean participationStatus,
        Integer activeParticipantCount,
        Double successRate,
        ChallengeVisibility visibility,
        UUID creatorId
) {
}