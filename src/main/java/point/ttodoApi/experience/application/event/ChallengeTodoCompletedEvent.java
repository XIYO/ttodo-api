package point.ttodoApi.experience.application.event;

import java.util.UUID;

public record ChallengeTodoCompletedEvent(
        UUID memberId,
        Long challengeId,
        String challengeTitle
) {
}
