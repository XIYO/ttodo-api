package point.zzicback.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AuthenticatedMember(
        @Schema(description = "회원 고유 ID", example = "b1a2c3d4-e5f6-7890-1234-56789abcdef0")
        String id,
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        @Schema(description = "닉네임", example = "홍길동")
        String nickname
) {
    public static AuthenticatedMember from(UUID id, String email, String nickname) {
        return new AuthenticatedMember(id.toString(), email, nickname);
    }
}
