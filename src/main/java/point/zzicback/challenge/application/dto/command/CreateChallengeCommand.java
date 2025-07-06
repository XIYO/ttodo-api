package point.zzicback.challenge.application.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.domain.ChallengeVisibility;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "챌린지 등록 요청")
public record CreateChallengeCommand(
        String title,
        String description,
        PeriodType periodType,
        ChallengeVisibility visibility,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxParticipants,
        UUID creatorId,
        List<Long> policyIds
) {}
