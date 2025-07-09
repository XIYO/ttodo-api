package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.challenge.application.dto.command.*;
import point.ttodoApi.challenge.application.dto.result.*;
import point.ttodoApi.challenge.application.mapper.ChallengeMapper;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.infrastructure.*;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.common.error.BusinessException;
import point.ttodoApi.common.error.ForbiddenException;
import point.ttodoApi.common.error.NotFoundException;
import point.ttodoApi.challenge.presentation.dto.response.ChallengePolicyResponse;
import point.ttodoApi.challenge.presentation.dto.response.InviteLinkResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeMapper challengeMapper;

    public Long createChallenge(CreateChallengeCommand command) {
        // 날짜 유효성 검증
        if (command.startDate().isAfter(command.endDate())) {
            throw new BusinessException("BIZ_001", "시작일이 종료일보다 늦을 수 없습니다");
        }
        
        // 챌린지 생성
        Challenge challenge;
        if (command.visibility() == ChallengeVisibility.PUBLIC) {
            challenge = Challenge.createPublicChallenge(
                command.title(),
                command.description(),
                command.periodType(),
                command.startDate(),
                command.endDate(),
                command.creatorId(),
                command.maxParticipants()
            );
        } else {
            challenge = Challenge.createInviteOnlyChallenge(
                command.title(),
                command.description(),
                command.periodType(),
                command.startDate(),
                command.endDate(),
                command.creatorId(),
                command.maxParticipants()
            );
        }
        
        Challenge saved = challengeRepository.save(challenge);
        
        // 정책 연결 (실제 구현 시)
        if (command.policyIds() != null && !command.policyIds().isEmpty()) {
            // TODO: 정책 연결 로직
        }
        
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public Page<ChallengeListResult> searchChallengesWithFilter(Member member, String keyword, 
                                                                String sort, Boolean join, 
                                                                Pageable pageable) {
        // 공개 챌린지만 검색
        Page<Challenge> challenges;
        
        if ("popular".equals(sort)) {
            challenges = challengeRepository.findByVisibilityOrderByParticipantCount(
                ChallengeVisibility.PUBLIC, keyword, pageable);
        } else {
            challenges = challengeRepository.findByVisibilityAndFilters(
                ChallengeVisibility.PUBLIC, keyword, null, pageable);
        }
        
        return challenges.map(challenge -> {
            ChallengeListResult result = challengeMapper.toListResult(challenge);
            
            // 인증된 사용자의 참여 상태 확인
            if (member != null) {
                boolean participated = challengeParticipationRepository
                    .existsByChallenge_IdAndMember_IdAndJoinOutIsNull(
                        challenge.getId(), member.getId());
                return new ChallengeListResult(
                    result.id(),
                    result.title(),
                    result.description(),
                    result.startDate(),
                    result.endDate(),
                    result.periodType(),
                    participated,
                    result.activeParticipantCount()
                );
            }
            return result;
        });
    }
    
    @Transactional(readOnly = true)
    public Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
            .orElseThrow(() -> new NotFoundException("챌린지를 찾을 수 없습니다"));
    }
    
    @Transactional(readOnly = true)
    public ChallengeResult getChallengeDetailForPublic(Long challengeId) {
        log.debug("공개 챌린지 상세 조회 시작 - challengeId: {}", challengeId);
        try {
            Challenge challenge = getChallenge(challengeId);
            ChallengeResult result = challengeMapper.toDetailResult(challenge);
            log.debug("공개 챌린지 상세 조회 성공 - challengeId: {}", challengeId);
            return result;
        } catch (Exception e) {
            log.error("공개 챌린지 상세 조회 실패 - challengeId: {}, error: {}", challengeId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public ChallengeResult getChallengeWithParticipation(Long challengeId, Member member) {
        Challenge challenge = getChallenge(challengeId);
        ChallengeResult result = challengeMapper.toDetailResult(challenge);
        
        // 참여 상태 확인
        boolean participated = challengeParticipationRepository
            .existsByChallenge_IdAndMember_IdAndJoinOutIsNull(
                challenge.getId(), member.getId());
        
        // 성공률 계산
        double successRate = calculateSuccessRate(challenge);
        
        return new ChallengeResult(
            result.id(),
            result.title(),
            result.description(),
            result.startDate(),
            result.endDate(),
            result.periodType(),
            participated,
            result.activeParticipantCount(),
            successRate,
            result.visibility(),
            result.creatorId()
        );
    }
    
    public void partialUpdateChallenge(Long challengeId, UpdateChallengeCommand command) {
        Challenge challenge = getChallenge(challengeId);
        
        // 권한 검증 - 생성자만 수정 가능
        // TODO: 현재 사용자 ID 가져와서 비교
        
        // 진행 중인 챌린지는 제한된 수정만 가능
        if (challenge.isActive()) {
            if (command.maxParticipants() != null && 
                command.maxParticipants() < challenge.getActiveParticipantCount()) {
                throw new BusinessException("BIZ_001", 
                    "현재 참여 인원보다 적은 수로 제한할 수 없습니다");
            }
        }
        
        challenge.update(
            command.title() != null ? command.title() : challenge.getTitle(),
            command.description() != null ? command.description() : challenge.getDescription(),
            command.periodType() != null ? command.periodType() : challenge.getPeriodType(),
            command.maxParticipants()
        );
    }
    
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = getChallenge(challengeId);
        
        // 참여자가 있는 챌린지는 삭제 불가
        if (challenge.getActiveParticipantCount() > 0) {
            throw new BusinessException("BIZ_001", "참여자가 있는 챌린지는 삭제할 수 없습니다");
        }
        
        // 진행 중인 챌린지는 삭제 불가
        if (challenge.isActive()) {
            throw new BusinessException("BIZ_001", "진행 중인 챌린지는 삭제할 수 없습니다");
        }
        
        challengeRepository.delete(challenge);
    }
    
    @Transactional(readOnly = true)
    public InviteLinkResponse getInviteLink(Long challengeId, UUID requesterId) {
        Challenge challenge = getChallenge(challengeId);
        
        // 생성자만 초대 링크 조회 가능
        if (!challenge.getCreatorId().equals(requesterId)) {
            throw new ForbiddenException("챌린지 생성자만 초대 링크를 생성할 수 있습니다");
        }
        
        if (challenge.getVisibility() != ChallengeVisibility.INVITE_ONLY) {
            throw new BusinessException("BIZ_001", "공개 챌린지는 초대 링크가 필요 없습니다");
        }
        
        return new InviteLinkResponse(
            challenge.getInviteCode(),
            "https://ttodo.com/challenges/invite/" + challenge.getInviteCode()
        );
    }
    
    @Transactional(readOnly = true)
    public List<ChallengePolicyResponse> getPolicyOptions() {
        // TODO: 실제 정책 데이터베이스에서 조회
        return List.of(
            new ChallengePolicyResponse(
                1L,
                "INACTIVITY_KICK",
                "비활동 자동 퇴장",
                "7일 이상 투두를 완료하지 않으면 자동 퇴장",
                "{\"maxInactiveDays\": 7}"
            ),
            new ChallengePolicyResponse(
                2L,
                "MIN_COMPLETION_RATE",
                "최소 완료율",
                "주간 완료율이 70% 미만이면 자동 퇴장",
                "{\"minCompletionRate\": 70, \"evaluationPeriodDays\": 7}"
            )
        );
    }
    
    /**
     * 챌린지 소유자 여부 확인 (Spring Security @PreAuthorize용)
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long challengeId, UUID memberId) {
        Challenge challenge = getChallenge(challengeId);
        return challenge.getCreatorId().equals(memberId);
    }

    private double calculateSuccessRate(Challenge challenge) {
        long activeParticipants = challenge.getActiveParticipantCount();
        if (activeParticipants == 0) {
            return 0.0;
        }
        
        LocalDate today = LocalDate.now();
        long completedCount = challengeTodoRepository
            .countCompletedParticipantsForDate(challenge.getId(), today);
        
        return (double) completedCount / activeParticipants * 100.0;
    }
}