package point.zzicback.challenge.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 챌린지 정책 옵션 응답 DTO
 */
@Schema(description = "챌린지 정책 옵션")
public record ChallengePolicyResponse(
    @Schema(description = "정책 ID", example = "1")
    Long id,
    
    @Schema(description = "정책 코드", example = "INACTIVITY_KICK")
    String code,
    
    @Schema(description = "정책 이름", example = "비활동 자동 퇴장")
    String name,
    
    @Schema(description = "정책 설명", example = "7일 이상 투두를 완료하지 않으면 자동 퇴장")
    String description,
    
    @Schema(description = "기본 설정값 (JSON)", example = "{\"maxInactiveDays\": 7}")
    String defaultConfig
) {}