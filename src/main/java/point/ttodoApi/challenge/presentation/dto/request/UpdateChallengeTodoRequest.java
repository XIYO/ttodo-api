package point.ttodoApi.challenge.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 투두 상태 수정 요청 DTO")
public record UpdateChallengeTodoRequest(
        @Schema(description = "완료 여부", example = "true")
        Boolean done
) {
}