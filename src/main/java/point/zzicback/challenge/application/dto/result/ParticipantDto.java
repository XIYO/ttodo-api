package point.zzicback.challenge.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantDto(
        UUID id,
        String email,
        String nickname,
        LocalDateTime joinedAt
) {
}