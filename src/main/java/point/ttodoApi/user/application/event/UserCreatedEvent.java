package point.ttodoApi.user.application.event;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email, String nickname) {
}
