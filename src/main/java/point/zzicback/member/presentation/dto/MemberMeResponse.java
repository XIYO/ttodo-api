package point.zzicback.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberMeResponse(
        @Schema(description = "사용자의 이메일 주소", example = "user@example.com")
        String email,

        @Schema(description = "사용자의 닉네임", example = "홍길동")
        String nickname
) {
}
