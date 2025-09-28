package point.ttodoApi.challenge.application.query;

import point.ttodoApi.challenge.domain.validation.required.ValidChallengeId;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 챌린지 참가 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 챌린지 참가 정보 조회 요청 캡슐화
 */
public record ChallengeParticipationQuery(
        @ValidChallengeId
        Long challengeId,
        
        @ValidUserId
        UUID userId
) {
}