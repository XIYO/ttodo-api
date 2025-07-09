package point.ttodoApi.challenge.application.dto.result;

import point.ttodoApi.challenge.domain.PeriodType;

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
