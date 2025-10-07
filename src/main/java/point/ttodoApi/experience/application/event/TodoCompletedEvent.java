package point.ttodoApi.experience.application.event;

import java.util.UUID;

public record TodoCompletedEvent(
        UUID userId,
        UUID todoId,
        Integer priorityId
) {
}
