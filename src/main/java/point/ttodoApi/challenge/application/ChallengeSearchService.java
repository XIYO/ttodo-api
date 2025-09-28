package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.challenge.domain.ChallengeVisibility;
import point.ttodoApi.challenge.domain.PeriodType;
import point.ttodoApi.challenge.infrastructure.*;
import point.ttodoApi.challenge.presentation.dto.request.ChallengeSearchRequest;

import java.time.LocalDate;
import java.util.*;

/**
 * 동적 쿼리를 사용한 Challenge 검색 서비스 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeSearchService {

  private final ChallengeRepository challengeRepository;

  /**
   * 챌린지 검색 - 다양한 조건으로 검색
   */
  public Page<Challenge> searchChallenges(ChallengeSearchRequest request, Pageable pageable) {
    // String을 enum으로 변환
    ChallengeVisibility visibility = request.getVisibility() != null ? 
            ChallengeVisibility.valueOf(request.getVisibility()) : null;
    PeriodType periodType = request.getPeriodType() != null ? 
            PeriodType.valueOf(request.getPeriodType()) : null;
            
    return challengeRepository.findChallengesWithDynamicQuery(
            request.getTitleKeyword(),
            request.getDescriptionKeyword(),
            visibility,
            periodType,
            request.getCreatorId(),
            request.isOngoingOnly(),
            request.isJoinableOnly(),
            pageable
    );
  }

  /**
   * 내가 생성한 챌린지 조회
   */
  public List<Challenge> getMyChallenges(UUID userId) {
    return challengeRepository.findByCreatorIdAndActiveTrue(userId);
  }

  /**
   * 공개되고 진행 중인 챌린지 조회
   */
  public Page<Challenge> getPublicOngoingChallenges(Pageable pageable) {
    LocalDate now = LocalDate.now();
    return challengeRepository.findPublicOngoingChallenges(now, pageable);
  }

}