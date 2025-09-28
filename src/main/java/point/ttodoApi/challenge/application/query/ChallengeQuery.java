package point.ttodoApi.challenge.application.query;

import point.ttodoApi.challenge.domain.validation.required.ValidChallengeId;

/**
 * 챌린지 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 챌린지 조회 요청 캡슐화
 */
public record ChallengeQuery(
        @ValidChallengeId
        Long challengeId
) {
}