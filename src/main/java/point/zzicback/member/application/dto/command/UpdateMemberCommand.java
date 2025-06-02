package point.zzicback.member.application.dto.command;

import java.util.UUID;

public record UpdateMemberCommand(UUID memberId, String nickname) {
}
