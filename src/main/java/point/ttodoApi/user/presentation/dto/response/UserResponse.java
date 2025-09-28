package point.ttodoApi.user.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "회원 정보 응답")
public record UserResponse(
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
        @Schema(description = "프로필 이미지 URL", example = "/api/user/123e4567-e89b-12d3-a456-426614174000/profile-image")
        String profileImageUrl
) {
  public static UserResponse from(point.ttodoApi.user.domain.User user, point.ttodoApi.profile.domain.Profile profile) {
    String profileImageUrl = user.getId() != null ?
            "/user/" + user.getId() + "/profile-image" : null;

    return new UserResponse(
            user.getId(),
            user.getEmail(),
            profile.getNickname(), // Profile.nickname 사용
            profile.getIntroduction() != null ? profile.getIntroduction() : "", // Profile.introduction
            profile.getTimeZone(), // Profile.timeZone
            profile.getLocale(), // Profile.locale
            profile.getTheme().name(), // Profile.theme
            profileImageUrl
    );
  }
}
