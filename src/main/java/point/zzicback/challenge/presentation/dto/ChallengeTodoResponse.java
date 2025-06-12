package point.zzicback.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.challenge.domain.PeriodType;

import java.time.LocalDate;

@Schema(description = "챌린지 투두 응답 DTO")
public record ChallengeTodoResponse(
        @Schema(description = "챌린지 투두 ID", example = "1")
        Long id,
        
        @Schema(description = "챌린지 제목", example = "매일 운동하기")
        String challengeTitle,
        
        @Schema(description = "챌린지 설명", example = "매일 30분 이상 운동하기")
        String challengeDescription,
        
        @Schema(description = "시작 날짜", example = "2024-01-01")
        LocalDate startDate,
        
        @Schema(description = "종료 날짜", example = "2024-01-31")
        LocalDate endDate,
        
        @Schema(description = "완료 여부", example = "false")
        Boolean done,
        
        @Schema(description = "영속성 여부", example = "true")
        Boolean isPersisted,
        
        @Schema(description = "기간 타입", example = "DAILY")
        PeriodType periodType
) {
}
