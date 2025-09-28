package point.ttodoApi.experience.application.event;

import java.util.UUID;

public record ChallengeTodoCompletedEvent(
        UUID userId,
        Long challengeId,
        String challengeTitle
) {
}
