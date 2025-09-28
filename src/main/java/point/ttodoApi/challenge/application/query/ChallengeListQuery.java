package point.ttodoApi.challenge.application.query;

import org.springframework.data.domain.Pageable;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 챌린지 목록 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 챌린지 목록 조회 요청 캡슐화
 */
public record ChallengeListQuery(
        @ValidUserId
        UUID userId,
        
        Pageable pageable
) {
}