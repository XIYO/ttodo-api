package point.zzicback.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.challenge.domain.PeriodType;

import java.time.LocalDate;

/**
 * 챌린지 요약 응답 (리스트)
 */
@Schema(description = "챌린지 요약 응답 DTO")
public record ChallengeResponse(
    @Schema(description = "챌린지 ID", example = "1")
    Long id,
    @Schema(description = "챌린지 제목", example = "하루 만보 걷기")
    String title,
    @Schema(description = "챌린지 설명", example = "매일 만보를 걸으면 인증!")
    String description,
    @Schema(description = "시작 날짜", example = "2024-01-01")
    LocalDate startDate,
    @Schema(description = "종료 날짜", example = "2024-01-07")
    LocalDate endDate,
    @Schema(description = "기간 타입", example = "DAILY")
    PeriodType periodType,
    @Schema(description = "내 참여 여부", example = "false")
    boolean participated,
    @Schema(description = "현재 참여자 수", example = "10")
    int participantCount
) {}