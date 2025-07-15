package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.challenge.domain.ChallengeStatus;
import point.ttodoApi.challenge.infrastructure.ChallengeRepository;
import point.ttodoApi.challenge.infrastructure.ChallengeSpecification;
import point.ttodoApi.common.specification.SpecificationBuilder;
import point.ttodoApi.common.specification.SortValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
    
    /**
     * 검색 요청 DTO
     */
    public static class ChallengeSearchRequest {
        private String titleKeyword;
        private String descriptionKeyword;
        private UUID creatorId;
        private String visibility;
        private String periodType;
        private LocalDate startDateFrom;
        private LocalDate startDateTo;
        private LocalDate endDateFrom;
        private LocalDate endDateTo;
        private boolean ongoingOnly;
        private boolean joinableOnly;
        
        // Getters and setters
        public String getTitleKeyword() { return titleKeyword; }
        public void setTitleKeyword(String titleKeyword) { this.titleKeyword = titleKeyword; }
        
        public String getDescriptionKeyword() { return descriptionKeyword; }
        public void setDescriptionKeyword(String descriptionKeyword) { this.descriptionKeyword = descriptionKeyword; }
        
        public UUID getCreatorId() { return creatorId; }
        public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
        
        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
        
        public String getPeriodType() { return periodType; }
        public void setPeriodType(String periodType) { this.periodType = periodType; }
        
        public LocalDate getStartDateFrom() { return startDateFrom; }
        public void setStartDateFrom(LocalDate startDateFrom) { this.startDateFrom = startDateFrom; }
        
        public LocalDate getStartDateTo() { return startDateTo; }
        public void setStartDateTo(LocalDate startDateTo) { this.startDateTo = startDateTo; }
        
        public LocalDate getEndDateFrom() { return endDateFrom; }
        public void setEndDateFrom(LocalDate endDateFrom) { this.endDateFrom = endDateFrom; }
        
        public LocalDate getEndDateTo() { return endDateTo; }
        public void setEndDateTo(LocalDate endDateTo) { this.endDateTo = endDateTo; }
        
        public boolean isOngoingOnly() { return ongoingOnly; }
        public void setOngoingOnly(boolean ongoingOnly) { this.ongoingOnly = ongoingOnly; }
        
        public boolean isJoinableOnly() { return joinableOnly; }
        public void setJoinableOnly(boolean joinableOnly) { this.joinableOnly = joinableOnly; }
    }
}