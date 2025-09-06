package point.ttodoApi.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.ttodoApi.challenge.domain.PeriodType;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "챌린지 상세 응답 DTO")
public record ChallengeDetailResponse(
        @Schema(description = "챌린지 ID", example = "1")
        Long id,
        @Schema(description = "챌린지 제목", example = "하루 만보 걷기")
        String title,
        @Schema(description = "챌린지 설명", example = "매일 만보를 걸으면 인증!")
        String description,
        @Schema(description = "시작 날짜", example = "2025-06-01")
        LocalDate startDate,
        @Schema(description = "종료 날짜", example = "2025-12-31")
        LocalDate endDate,
        @Schema(description = "기간 타입", example = "DAILY")
        PeriodType periodType,
        @Schema(description = "내 참여 여부", example = "false")
        boolean participated,
        @Schema(description = "현재 참여자 수", example = "10")
        int participantCount,
        @Schema(description = "성공률 (투두 완료율)", example = "0.75")
        float successRate,
        @Schema(description = "완료한 참여자 수", example = "10")
        int completedCount,
        @Schema(description = "전체 참여자 수", example = "30")
        int totalCount,
        @Schema(description = "참여자 목록 (페이지네이션)")
        List<ParticipantResponse> participants
) {
}