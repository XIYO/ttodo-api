package point.zzicback.profile.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.profile.domain.Theme;

/**
 * 프로필 정보 응답 DTO
 */
@Schema(description = "프로필 정보 응답")
public record ProfileResponse(
        @Schema(description = "닉네임")
        String nickname,
        
        @Schema(description = "소개글")
        String introduction,
        
        @Schema(description = "테마 설정")
        Theme theme,
        
        @Schema(description = "프로필 이미지 존재 여부")
        boolean hasProfileImage
) {
}