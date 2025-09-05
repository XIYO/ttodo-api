package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.challenge.presentation.dto.request.ChallengeSearchRequest;
import point.ttodoApi.challenge.infrastructure.*;
import point.ttodoApi.shared.specification.*;

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
    private final ChallengeSpecification challengeSpecification;
    private final SortValidator sortValidator;
    
    /**
     * 챌린지 검색 - 다양한 조건으로 검색
     */
    public Page<Challenge> searchChallenges(ChallengeSearchRequest request, Pageable pageable) {
        // 정렬 필드 검증
        sortValidator.validateSort(pageable.getSort(), challengeSpecification);
        
        // SpecificationBuilder를 사용한 동적 쿼리 구성
        SpecificationBuilder<Challenge> builder = new SpecificationBuilder<>(challengeSpecification);
        
        Specification<Challenge> spec = builder
                // 기본 조건
                .with("active", true)
                
                // 선택적 조건들
                .withLike("title", request.getTitleKeyword())
                .withLike("description", request.getDescriptionKeyword())
                .with("visibility", request.getVisibility())
                .with("periodType", request.getPeriodType())
                .withDateRange("startDate", request.getStartDateFrom(), request.getStartDateTo())
                .withDateRange("endDate", request.getEndDateFrom(), request.getEndDateTo())
                
                // 생성자 필터
                .withIf(request.getCreatorId() != null, "creatorId", request.getCreatorId())
                
                // 진행 중인 챌린지만
                .withIf(request.isOngoingOnly(), builder2 -> 
                    builder2.lessThanOrEqual("startDate", LocalDate.now())
                           .greaterThanOrEqual("endDate", LocalDate.now()))
                
                // 참여 가능한 챌린지 (최대 인원 미달)
                .withIf(request.isJoinableOnly(), builder2 ->
                    builder2.or(spec2 -> spec2
                        .isNull("maxParticipants")
                        .lessThan("currentParticipants", "maxParticipants")))
                
                .build();
        
        return challengeRepository.findAll(spec, pageable);
    }
    
    /**
     * 내가 생성한 챌린지 조회
     */
    public List<Challenge> getMyChallenges(UUID memberId) {
        SpecificationBuilder<Challenge> builder = new SpecificationBuilder<>(challengeSpecification);
        
        Specification<Challenge> spec = builder
                .with("creatorId", memberId)
                .with("active", true)
                .build();
        
        return challengeRepository.findAll(spec);
    }
    
    /**
     * 공개되고 진행 중인 챌린지 조회
     */
    public Page<Challenge> getPublicOngoingChallenges(Pageable pageable) {
        SpecificationBuilder<Challenge> builder = new SpecificationBuilder<>(challengeSpecification);
        
        Specification<Challenge> spec = builder
                .with("active", true)
                .with("visibility", "PUBLIC")
                .lessThanOrEqual("startDate", LocalDate.now())
                .greaterThanOrEqual("endDate", LocalDate.now())
                .build();
        
        return challengeRepository.findAll(spec, pageable);
    }
    
}