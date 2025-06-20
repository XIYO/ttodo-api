package point.zzicback.member.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원 정보 수정 요청 DTO
 */
@Schema(description = "회원 정보 수정 요청 DTO")
public record UpdateMemberRequest(
        @Schema(description = "회원 닉네임", example = "새로운닉네임")
        String nickname,
        @Schema(description = "소개글", example = "매일 조금씩 성장하는 것이 목표입니다.")
        String introduction
) {}