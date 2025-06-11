package point.zzicback.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "회원 정보 응답")
public record MemberResponse(
        @Schema(description = "회원 ID")
        UUID id,
        @Schema(description = "회원 이메일")
        String email,
        @Schema(description = "회원 닉네임")
        String nickname
) {} // 비밀번호는 보안상 응답에 포함시키지 않음
