package point.ttodoApi.challenge.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import point.ttodoApi.challenge.domain.PeriodType;
import point.ttodoApi.challenge.domain.validation.required.ValidChallengeTitle;
import point.ttodoApi.challenge.domain.validation.optional.OptionalChallengeDescription;

/**
 * 챌린지 수정 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 챌린지 수정 요청 캡슐화
 */
@Schema(description = "챌린지 수정 요청")
public record UpdateChallengeCommand(
        @ValidChallengeTitle
        @Schema(description = "챌린지 제목", example = "하루 만보 걷기")
        String title,
        
        @OptionalChallengeDescription
        @Schema(description = "챌린지 설명", example = "매일 만보를 걸으면 인증!")
        String description,
        
        @Schema(description = "챌린지 기간 타입", example = "DAILY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
        PeriodType periodType,
        
        @Positive(message = "최대 참가자 수는 양수여야 합니다")
        @Schema(description = "최대 참여 인원", example = "50")
        Integer maxParticipants
) {
}
