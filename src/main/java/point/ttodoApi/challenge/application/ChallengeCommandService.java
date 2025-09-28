package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.challenge.application.command.CreateChallengeCommand;
import point.ttodoApi.challenge.application.command.UpdateChallengeCommand;
import point.ttodoApi.challenge.application.result.ChallengeResult;
import point.ttodoApi.challenge.application.ChallengeService;
import point.ttodoApi.challenge.infrastructure.ChallengeRepository;
import point.ttodoApi.challenge.domain.Challenge;

import jakarta.validation.Valid;

/**
 * Challenge Command Service
 * TTODO 아키텍처 패턴: Command 처리 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class ChallengeCommandService {
    
    private final ChallengeService challengeService; // 기존 서비스 위임
    private final ChallengeRepository challengeRepository;

    /**
     * 챌린지 생성
     */
    public ChallengeResult createChallenge(@Valid CreateChallengeCommand command) {
        // 기존 서비스로 위임 (향후 TTODO 아키텍처 패턴으로 리팩토링)
        Long challengeId = challengeService.createChallenge(command);
        
        // ChallengeResult로 변환
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalStateException("생성된 챌린지를 찾을 수 없습니다: " + challengeId));
            
        return new ChallengeResult(
            challenge.getId(),
            challenge.getTitle(),
            challenge.getDescription(),
            challenge.getStartDate(),
            challenge.getEndDate(),
            challenge.getPeriodType(),
            null, // participationStatus - 생성 시점에서는 알 수 없음
            0, // activeParticipantCount - 새로 생성된 챌린지
            null, // successRate - 아직 시작하지 않음
            challenge.getVisibility(),
            challenge.getCreatorId()
        );
    }

    /**
     * 챌린지 수정
     */
    public ChallengeResult updateChallenge(@Valid UpdateChallengeCommand command) {
        // 기존 서비스로 위임 (향후 TTODO 아키텍처 패턴으로 리팩토링)
        // TODO: ChallengeService.updateChallenge 메서드 구현 확인 필요
        throw new UnsupportedOperationException("updateChallenge 메서드는 추후 구현 예정");
    }
}