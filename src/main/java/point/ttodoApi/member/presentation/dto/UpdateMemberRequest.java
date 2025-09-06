package point.ttodoApi.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 회원 정보 수정 요청 DTO
 */
@Schema(description = "회원 정보 수정 요청 DTO")
public record UpdateMemberRequest(
        @Schema(description = "회원 닉네임", example = "새로운닉네임")
        @Size(max = 255, message = "닉네임은 255자를 초과할 수 없습니다.")
        String nickname,

        @Schema(description = "소개글", example = "매일 조금씩 성장하는 것이 목표입니다.")
        @Size(max = 500, message = "소개글은 500자를 초과할 수 없습니다.")
        String introduction
) {
}