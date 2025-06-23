package point.zzicback.experience.application.event;

import java.util.UUID;

public record ChallengeTodoCompletedEvent(
        UUID memberId,
        Long challengeId,
        String challengeTitle
) {
}
