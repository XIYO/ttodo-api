package point.ttodoApi.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import point.ttodoApi.challenge.domain.PeriodType;

@Schema(description = "챌린지 수정 요청 DTO")
public record UpdateChallengeRequest(
        @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하여야 합니다")
        @Schema(description = "챌린지 제목", example = "하루 만보 걷기")
        String title,

        @Size(max = 65535, message = "설명은 65535자 이하여야 합니다")
        @Schema(description = "챌린지 설명", example = "매일 만보를 걸으면 인증!")
        String description,

        @Schema(description = "챌린지 기간 타입", example = "DAILY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
        PeriodType periodType,

        @Min(value = 1, message = "최대 참여 인원은 1명 이상이어야 합니다")
        @Max(value = 1000, message = "최대 참여 인원은 1000명 이하여야 합니다")
        @Schema(description = "최대 참여 인원", example = "10")
        Integer maxParticipants
) {
}