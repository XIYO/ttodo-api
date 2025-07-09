package point.ttodoApi.member.application.event;

import java.util.UUID;

public record MemberCreatedEvent(UUID memberId, String email, String nickname) {
}
