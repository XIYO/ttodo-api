package point.zzicback.challenge.application.dto.result;

import point.zzicback.challenge.domain.PeriodType;

import java.time.LocalDate;

public record ChallengeTodoResult(
        Long id,
        String challengeTitle,
        String challengeDescription,
        LocalDate startDate,
        LocalDate endDate,
        Boolean done,
        Boolean isPersisted,
        PeriodType periodType
) {
}
