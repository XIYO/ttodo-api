package point.ttodoApi.auth.application.query;

import point.ttodoApi.auth.domain.validation.required.ValidDeviceId;

/**
 * 개발용 토큰 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 개발용 토큰 요청 캡슐화
 */
public record DevTokenQuery(
        @ValidDeviceId
        String deviceId
) {
}