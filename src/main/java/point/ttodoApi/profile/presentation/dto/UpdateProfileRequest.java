package point.ttodoApi.profile.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import point.ttodoApi.profile.domain.Theme;

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

        @Schema(description = "시간대 (IANA Time Zone)", example = "Asia/Seoul")
        @Size(max = 50, message = "시간대는 50자를 초과할 수 없습니다.")
        String timeZone,

        @Schema(description = "언어 설정 (BCP 47 언어 태그)", example = "ko-KR")
        @Size(max = 10, message = "언어 설정은 10자를 초과할 수 없습니다.")
        String locale,

        @Schema(description = "테마 설정")
        Theme theme
) {
}