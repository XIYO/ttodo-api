package point.ttodoApi.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 초대 코드로 챌린지 참여 요청 DTO
 */
@Schema(description = "초대 코드로 챌린지 참여 요청")
public record JoinByInviteRequest(
        @NotBlank
        @Schema(description = "초대 코드", example = "ABC12345")
        String inviteCode
) {
}