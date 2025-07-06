package point.zzicback.profile.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import point.zzicback.profile.domain.Theme;

/**
 * 프로필 정보 수정 요청 DTO
 */
@Schema(description = "프로필 정보 수정 요청 DTO")
public record UpdateProfileRequest(
        @Schema(description = "닉네임", example = "새로운닉네임")
        @Size(max = 255, message = "닉네임은 255자를 초과할 수 없습니다.")
        String nickname,
        
        @Schema(description = "소개글", example = "매일 조금씩 성장하는 것이 목표입니다.")
        @Size(max = 500, message = "소개글은 500자를 초과할 수 없습니다.")
        String introduction,
        
        @Schema(description = "테마 설정")
        Theme theme
) {
}