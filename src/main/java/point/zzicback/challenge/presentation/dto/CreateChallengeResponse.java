package point.zzicback.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 생성 응답 DTO")
public record CreateChallengeResponse(
        @Schema(description = "생성된 챌린지 ID", example = "1")
        Long challengeId,
        
        @Schema(description = "응답 메시지", example = "챌린지가 성공적으로 생성되었습니다.")
        String message
) {
    public static CreateChallengeResponse of(Long challengeId) {
        return new CreateChallengeResponse(challengeId, "챌린지가 성공적으로 생성되었습니다.");
    }
}
