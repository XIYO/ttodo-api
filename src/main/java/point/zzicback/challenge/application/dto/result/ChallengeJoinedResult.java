package point.zzicback.challenge.application.dto.result;

import point.zzicback.challenge.domain.PeriodType;

import java.time.LocalDate;

public record ChallengeJoinedResult(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PeriodType periodType,
        boolean participationStatus
) {
}