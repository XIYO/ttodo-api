package point.ttodoApi.experience.application.event;

import java.util.UUID;

public record TodoUncompletedEvent(
        UUID userId,
        Long todoId,
        String todoTitle
) {
}
