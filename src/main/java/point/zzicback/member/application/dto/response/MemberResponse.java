package point.zzicback.member.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.member.domain.Member;

public record MemberResponse(
        @Schema(description = "사용자의 이메일 주소", example = "user@example.com")
        String email,

        @Schema(description = "사용자의 닉네임", example = "홍길동")
        String nickname
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getEmail(), member.getNickname());
    }
}
