package point.ttodoApi.experience.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 레벨/경험치 응답 DTO")
public record MemberLevelResponse(
        @Schema(description = "현재 레벨", example = "5")
        int currentLevel,

        @Schema(description = "레벨명", example = "나무늘보")
        String levelName,

        @Schema(description = "현재 보유 경험치", example = "850")
        int currentExp,

        @Schema(description = "현재 레벨의 최소 필요 경험치", example = "700")
        int currentLevelMinExp,

        @Schema(description = "다음 레벨까지 필요한 경험치", example = "150")
        int expToNextLevel,

        @Schema(description = "현재 레벨에서 획득한 경험치", example = "150")
        int currentLevelProgress,

        @Schema(description = "현재 레벨에서 필요한 총 경험치", example = "300")
        int currentLevelTotal
) {
}
