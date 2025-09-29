package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.challenge.application.query.ChallengeListQuery;
import point.ttodoApi.challenge.application.query.ChallengeQuery;
import point.ttodoApi.challenge.application.result.ChallengeResult;
import point.ttodoApi.challenge.presentation.dto.request.ChallengeSearchRequest;
import point.ttodoApi.challenge.domain.Challenge;

import jakarta.validation.Valid;

/**
 * Challenge Query Service
 * TTODO 아키텍처 패턴: Query 처리 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class ChallengeQueryService {
    
    private final ChallengeSearchService challengeSearchService; // 기존 서비스 위임

    /**
     * 챌린지 목록 조회
     */
    public Page<ChallengeResult> getChallenges(@Valid ChallengeListQuery query) {
        // 기본 검색 요청 생성
        ChallengeSearchRequest request = new ChallengeSearchRequest();
        // 기존 서비스로 위임 (향후 TTODO 아키텍처 패턴으로 리팩토링)
        return challengeSearchService.searchChallenges(request, query.pageable())
                .map(this::toChallengeResult);
    }

    /**
     * 챌린지 상세 조회
     */
    public ChallengeResult getChallenge(@Valid ChallengeQuery query) {
        // Challenge search service integration pending
        throw new UnsupportedOperationException("getChallenge method implementation pending");
    }
    
    private ChallengeResult toChallengeResult(Challenge challenge) {
        // 임시 매퍼 - 향후 MapStruct로 대체
        return new ChallengeResult(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                false, // participationStatus
                challenge.getCurrentParticipants(),
                0.0, // successRate
                challenge.getVisibility(),
                challenge.getCreatorId()
        );
    }
}