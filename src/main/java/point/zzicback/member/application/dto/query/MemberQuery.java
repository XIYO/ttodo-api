package point.zzicback.member.application.dto.query;

import java.util.UUID;

public record MemberQuery(UUID memberId) {
public static MemberQuery of(UUID memberId) {
  return new MemberQuery(memberId);
}
}
