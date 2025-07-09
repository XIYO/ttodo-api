package point.ttodoApi.challenge.application.dto.result;

import point.ttodoApi.challenge.domain.PeriodType;

import java.time.LocalDate;
import java.util.List;

public record ChallengeDetailResult(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PeriodType periodType,
        Boolean participationStatus,
        Integer activeParticipantCount,
        List<ParticipantResult> participants
) {
}