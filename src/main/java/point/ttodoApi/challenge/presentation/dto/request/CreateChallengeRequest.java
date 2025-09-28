package point.ttodoApi.challenge.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.shared.validation.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "챌린지 생성 요청 DTO")
public record CreateChallengeRequest(
        @NotBlank
        @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하여야 합니다")
        @Schema(description = "챌린지 제목", example = "하루 만보 걷기")
        @NoSqlInjection
        String title,

        @Size(max = 65535, message = "설명은 65535자 이하여야 합니다")
        @Schema(description = "챌린지 설명", example = "매일 만보를 걸으면 인증!")
        @SanitizeHtml(mode = SanitizeHtml.SanitizeMode.STANDARD)
        String description,

        @Schema(description = "챌린지 기간 타입", example = "DAILY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
        PeriodType periodType,

        @Schema(description = "챌린지 가시성", example = "PUBLIC", allowableValues = {"PUBLIC", "INVITE_ONLY"})
        ChallengeVisibility visibility,

        @FutureOrPresent
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "시작일", example = "2024-01-01")
        LocalDate startDate,

        @Future
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "종료일", example = "2024-12-31")
        LocalDate endDate,

        @Min(1)
        @Max(1000)
        @Schema(description = "최대 참여 인원 (선택사항)", example = "100")
        Integer maxParticipants,

        @Schema(description = "정책 ID 목록 (선택사항)", example = "[1, 2, 3]")
        List<Long> policyIds
) {
}