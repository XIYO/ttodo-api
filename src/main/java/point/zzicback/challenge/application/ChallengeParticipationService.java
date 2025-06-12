package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.ChallengeParticipationRepository;
import point.zzicback.challenge.infrastructure.ChallengeTodoRepository;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeParticipationService {
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeService challengeService;

    // 참여
    public ChallengeParticipation joinChallenge(Long challengeId, Member member) {
        Challenge challenge = challengeService.findById(challengeId);

        if (participationRepository.existsByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)) {
            throw new BusinessException("이미 참여중인 챌린지입니다.");
        }

        ChallengeParticipation participation = ChallengeParticipation.builder()
                .challenge(challenge)
                .member(member)
                .build();

        return participationRepository.save(participation);
    }

    // 중도하차 (soft delete)
    public void leaveChallenge(Long challengeId, Member member) {
        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)
                .orElseThrow(() -> new BusinessException("참여하지 않은 챌린지입니다."));

        participation.leaveChallenge();
        participationRepository.save(participation);
    }

    // 참여자가 챌린지 간격에 의해 해야할 챌린지 투두를 출력
    public List<ChallengeTodo> getChallengeTodos(Member member) {
        return participationRepository.findByMemberAndJoinOutIsNull(member)
                .stream()
                .map(participation -> challengeTodoRepository.findByChallengeParticipation(participation))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

