package point.ttodoApi.experience.application.query;

import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 회원 레벨 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 회원 레벨 조회 요청 캡슐화
 */
public record UserLevelQuery(
        @ValidUserId
        UUID userId
) {
}