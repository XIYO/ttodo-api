package point.zzicback.member.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 회원 정보 수정 요청 DTO
 */
@Schema(description = "회원 정보 수정 요청 DTO")
public record UpdateMemberRequest(
        @NotBlank
        @Schema(description = "회원 닉네임", example = "새로운닉네임")
        String nickname
) {}