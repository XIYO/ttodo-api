package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.application.mapper.ChallengeApplicationMapper;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.infrastructure.*;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeApplicationMapper challengeApplicationMapper;

    //챌린지 생성
    public Long createChallenge(CreateChallengeCommand command) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = calculateEndDate(startDate, command.periodType());
        
        Challenge challenge = Challenge.builder()
                .title(command.title())
                .description(command.description())
                .periodType(command.periodType())
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return challengeRepository.save(challenge).getId();
    }
    
    private LocalDate calculateEndDate(LocalDate startDate, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> startDate.plusDays(1);
            case WEEKLY -> startDate.plusWeeks(1);
            case MONTHLY -> startDate.plusMonths(1);
        };
    }

    //챌린지 목록 조회
    @Transactional(readOnly = true)
    public List<ChallengeDto> getChallenges() {
        return challengeApplicationMapper.toChallengeDto(challengeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<ChallengeJoinedDto> getChallengesByMember(Member member) {
        // 1. 모든 챌린지 조회
        List<Challenge> allChallenges = challengeRepository.findAll();

        // 2. 회원이 참여중인 챌린지 ID 목록 조회
        List<Long> participatedChallengeIds = challengeParticipationRepository.findByMember(member)
                .stream()
                .map(participation -> participation.getChallenge().getId())
                .toList();

        // 3. 모든 챌린지에 대해 회원 참여 여부를 포함한 응답 생성
        return allChallenges.stream()
                .map(challenge -> new ChallengeJoinedDto(
                        challenge.getId(),
                        challenge.getTitle(),
                        challenge.getDescription(),
                        challenge.getStartDate(),
                        challenge.getEndDate(),
                        challenge.getPeriodType(),
                        participatedChallengeIds.contains(challenge.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Challenge findById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
    }

    public ChallengeDto getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .map(challengeApplicationMapper::toChallengeDto)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
    }


    //챌린지 업데이트
    public void updateChallenge(Long challengeId, UpdateChallengeCommand command) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
        challenge.update(command.title(), command.description(), command.periodType());
        challengeRepository.save(challenge);
    }

    //챌린지 삭제
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
        challengeRepository.delete(challenge);
    }

    // 모든 챌린지와 각 챌린지별 참여자 목록 조회
    @Transactional(readOnly = true)
    public List<ChallengeDetailDto> getAllChallengesWithParticipants() {
        // 챌린지와 참여자 정보를 함께 조회 (N+1 문제 방지)
        List<Challenge> allChallenges = challengeRepository.findAllWithParticipations();

        // 매퍼를 통해 엔티티 목록을 응답 객체 목록으로 변환
        return challengeApplicationMapper.toChallengeDetailDto(allChallenges);
    }
}
