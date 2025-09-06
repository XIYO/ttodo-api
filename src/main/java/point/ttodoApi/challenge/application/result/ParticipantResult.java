package point.ttodoApi.challenge.application.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantResult(
        UUID id,
        String email,
        String nickname,
        LocalDateTime joinedAt
) {
}