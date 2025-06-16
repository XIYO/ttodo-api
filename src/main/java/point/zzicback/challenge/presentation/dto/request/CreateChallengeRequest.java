package point.zzicback.challenge.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import point.zzicback.challenge.domain.PeriodType;

@Schema(description = "챌린지 생성 요청 DTO")
public record CreateChallengeRequest(
    @NotBlank
    @Schema(description = "챌린지 제목", example = "하루 만보 걷기")
    String title,
    
    @Schema(description = "챌린지 설명", example = "매일 만보를 걸으면 인증!")
    String description,
    
    @NotNull
    @Schema(description = "챌린지 기간 타입", example = "DAILY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
    PeriodType periodType
) {}