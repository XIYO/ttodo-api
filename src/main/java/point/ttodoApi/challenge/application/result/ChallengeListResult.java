package point.ttodoApi.challenge.application.result;

import point.ttodoApi.challenge.domain.PeriodType;

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
