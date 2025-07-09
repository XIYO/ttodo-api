package point.ttodoApi.profile.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import point.ttodoApi.profile.domain.Theme;

/**
 * 프로필 정보 응답 DTO
 */
@Schema(description = "프로필 정보 응답")
public record ProfileResponse(
        @Schema(description = "닉네임")
        String nickname,
        
        @Schema(description = "소개글")
        String introduction,
        
        @Schema(description = "시간대 (IANA Time Zone)", example = "Asia/Seoul")
        String timeZone,
        
        @Schema(description = "언어 설정 (BCP 47 언어 태그)", example = "ko-KR")
        String locale,
        
        @Schema(description = "테마 설정")
        Theme theme,
        
        @Schema(description = "이미지 URL (이미지가 있는 경우)")
        String imageUrl
) {
}