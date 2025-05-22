package point.zzicback.member.domain;

import java.util.UUID;

public record AuthenticatedMember(
        String id,
        String email,
        String nickname
) {
    public static AuthenticatedMember from(UUID id, String email, String nickname) {
        return new AuthenticatedMember(id.toString(), email, nickname);
    }
}
