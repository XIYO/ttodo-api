package point.zzicback.challenge.application.dto.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import point.zzicback.challenge.domain.PeriodType;

import java.time.LocalDate;

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
        Float successRate
) {
}