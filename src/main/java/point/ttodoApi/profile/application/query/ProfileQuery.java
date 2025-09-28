package point.ttodoApi.profile.application.query;

import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 프로필 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 프로필 조회 요청 캡슐화
 */
public record ProfileQuery(
        @ValidUserId
        UUID userId
) {
}