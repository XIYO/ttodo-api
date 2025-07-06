package point.zzicback.challenge.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 챌린지 초대 링크 응답 DTO
 */
@Schema(description = "챌린지 초대 링크 응답")
public record InviteLinkResponse(
    @Schema(description = "초대 코드", example = "ABC12345")
    String inviteCode,
    
    @Schema(description = "초대 링크 URL", example = "https://zzic.com/challenges/invite/ABC12345")
    String inviteUrl
) {}