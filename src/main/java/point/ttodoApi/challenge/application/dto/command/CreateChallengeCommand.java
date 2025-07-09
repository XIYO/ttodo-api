package point.ttodoApi.challenge.application.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import point.ttodoApi.challenge.domain.*;

import java.time.LocalDate;
import java.util.*;

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
