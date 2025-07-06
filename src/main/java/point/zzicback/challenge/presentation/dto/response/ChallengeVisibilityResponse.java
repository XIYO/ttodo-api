package point.zzicback.challenge.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 챌린지 가시성 옵션 응답 DTO
 */
@Schema(description = "챌린지 가시성 옵션")
public record ChallengeVisibilityResponse(
    @Schema(description = "가시성 코드", example = "PUBLIC")
    String code,
    
    @Schema(description = "가시성 설명", example = "공개")
    String description
) {}