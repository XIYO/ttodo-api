package point.ttodoApi.profile.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 프로필 이미지 업로드 응답 DTO
 */
@Schema(description = "프로필 이미지 업로드 응답")
public record ProfileImageUploadResponse(
        @Schema(description = "업로드된 프로필 이미지 URL", example = "/user/123e4567-e89b-12d3-a456-426614174000/profile/image")
        String imageUrl
) {
}