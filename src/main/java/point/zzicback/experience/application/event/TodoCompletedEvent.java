package point.zzicback.experience.application.event;

import java.util.UUID;

public record TodoCompletedEvent(
        UUID memberId,
        Long todoId,
        String todoTitle
) {
}
