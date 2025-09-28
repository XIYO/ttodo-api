package point.ttodoApi.challenge.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.domain.validation.required.ValidChallengeTitle;
import point.ttodoApi.challenge.domain.validation.required.ValidChallengeDates;
import point.ttodoApi.challenge.domain.validation.optional.OptionalChallengeDescription;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.time.LocalDate;
import java.util.*;

/**
 * 챌린지 생성 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 챌린지 생성 요청 캡슐화
 */
@Schema(description = "챌린지 등록 요청")
public record CreateChallengeCommand(
        @ValidChallengeTitle
        String title,
        
        @OptionalChallengeDescription
        String description,
        
        PeriodType periodType,
        
        ChallengeVisibility visibility,
        
        @ValidChallengeDates
        LocalDate startDate,
        
        @ValidChallengeDates
        LocalDate endDate,
        
        @Positive(message = "최대 참가자 수는 양수여야 합니다")
        Integer maxParticipants,
        
        @ValidUserId
        UUID creatorId,
        
        List<Long> policyIds
) {
}
