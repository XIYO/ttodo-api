package point.ttodoApi.member.application.dto.result;

import java.util.UUID;

/**
 * 회원 정보 조회 결과 DTO
 */
public record MemberResult(
        UUID id,
        String email,
        String nickname
) {}