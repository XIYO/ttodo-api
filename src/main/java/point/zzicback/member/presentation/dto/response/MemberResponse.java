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
        String nickname,
        @Schema(description = "소개글")
        String introduction,
        @Schema(description = "사용자 타임존")
        String timeZone,
        @Schema(description = "사용자 로케일")
        String locale,
        @Schema(description = "테마 설정")
        String theme,
        @Schema(description = "프로필 이미지 URL", example = "/api/members/123e4567-e89b-12d3-a456-426614174000/profile-image")
        String profileImageUrl
) {
    public static MemberResponse from(point.zzicback.member.domain.Member member) {
        String profileImageUrl = member.getId() != null ? 
            "/members/" + member.getId() + "/profile-image" : null;
        
        return new MemberResponse(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            "", // 기본 소개글
            "Asia/Seoul", // 기본 타임존
            "ko-KR", // 기본 로케일
            "LIGHT", // 기본 테마
            profileImageUrl
        );
    }
}
