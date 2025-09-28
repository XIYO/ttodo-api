package point.ttodoApi.challenge.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "챌린지 참여자 응답 DTO")
public record ParticipantResponse(
        @Schema(description = "회원 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "회원 이메일", example = "user@example.com")
        String email,

        @Schema(description = "회원 닉네임", example = "ttodo_user")
        String nickname,

        @Schema(description = "참여 일시", example = "2024-01-01T12:00:00")
        LocalDateTime joinedAt
) {
}