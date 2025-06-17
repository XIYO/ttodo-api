package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.dto.command.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.application.mapper.ChallengeMapper;
import point.zzicback.challenge.domain.*;
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
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeMapper challengeMapper;

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

    @Transactional(readOnly = true)
    public Page<Challenge> getChallenges(Pageable pageable) {
        return challengeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Challenge> searchChallenges(String keyword, String sort, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "popular".equals(sort) 
                ? challengeRepository.findAllOrderedByPopularity(pageable)
                : challengeRepository.findAll(pageable);
        }
        return "popular".equals(sort)
            ? challengeRepository.searchByKeywordOrderedByPopularity(keyword.trim(), pageable)
            : challengeRepository.searchByKeyword(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<ChallengeListResult> searchChallengesWithFilter(Member member, String keyword, String sort, Boolean join, Pageable pageable) {
        Page<Challenge> challengePage;
        if (keyword == null || keyword.trim().isEmpty()) {
            challengePage = "popular".equals(sort)
                ? challengeRepository.findAllOrderedByPopularity(pageable)
                : challengeRepository.findAll(pageable);
        } else {
            challengePage = "popular".equals(sort)
                ? challengeRepository.searchByKeywordOrderedByPopularity(keyword.trim(), pageable)
                : challengeRepository.searchByKeyword(keyword.trim(), pageable);
        }
        
        List<Long> participatedChallengeIds = challengeParticipationRepository.findByMemberAndJoinOutIsNull(member)
                .stream()
                .map(participation -> participation.getChallenge().getId())
                .toList();
        
        List<ChallengeListResult> filteredChallenges = challengePage.getContent().stream()
                .map(challenge -> {
                    boolean isParticipated = participatedChallengeIds.contains(challenge.getId());
                    int activeParticipantCount = (int) challenge.getParticipations().stream()
                            .filter(participation -> participation.getJoinOut() == null)
                            .count();
                    return challengeMapper.toListResult(challenge, isParticipated, activeParticipantCount);
                })
                .filter(challengeDto -> join == null || join.equals(challengeDto.participationStatus()))
                .toList();
        
        return new PageImpl<>(filteredChallenges, pageable, filteredChallenges.size());
    }

    @Transactional(readOnly = true)
    public List<ChallengeJoinedResult> getChallengesByMember(Member member) {
        List<Challenge> allChallenges = challengeRepository.findAll();
        List<Long> participatedChallengeIds = challengeParticipationRepository.findByMemberAndJoinOutIsNull(member)
                .stream()
                .map(participation -> participation.getChallenge().getId())
                .toList();
        
        return allChallenges.stream()
                .map(challenge -> challengeMapper.toJoinedResult(
                        challenge,
                        participatedChallengeIds.contains(challenge.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Challenge findById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
    }

    public Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
    }

    @Transactional(readOnly = true)
    public ChallengeResult getChallengeWithParticipation(Long challengeId, Member member) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
        
        List<Long> participatedChallengeIds = challengeParticipationRepository.findByMemberAndJoinOutIsNull(member)
                .stream()
                .map(participation -> participation.getChallenge().getId())
                .toList();
        
        // 현재 활동 중인 참여자 수
        int activeParticipantCount = (int) challenge.getParticipations().stream()
                .filter(participation -> participation.getJoinOut() == null)
                .count();
        
        // 해당 챌린지에 참여한 전체 사람 수 (탈퇴자 포함)
        int totalParticipantCount = challenge.getParticipations().size();
        
        // 챌린지 투두를 완료한 참여자 수
        long completedParticipantCount = challengeTodoRepository.countCompletedParticipantsByChallengeId(challengeId);
        
        // 성공률 계산 (챌린지 투두를 완료한 참여자 / 전체 참여자)
        float successRate = totalParticipantCount > 0 ? 
                Math.round((float) completedParticipantCount / totalParticipantCount * 100) / 100.0f : 0.0f;
        
        boolean isParticipated = participatedChallengeIds.contains(challenge.getId());
        return challengeMapper.toResult(
                challenge,
                isParticipated,
                activeParticipantCount,
                successRate,
                (int) completedParticipantCount,
                totalParticipantCount
        );
    }

    public void updateChallenge(Long challengeId, UpdateChallengeCommand command) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
        challenge.update(command.title(), command.description(), command.periodType());
        challengeRepository.save(challenge);
    }

    public void deleteChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
        challengeRepository.delete(challenge);
    }

    @Transactional(readOnly = true)
    public Page<Challenge> getAllChallengesWithParticipants(Pageable pageable) {
        return challengeRepository.findAllWithParticipations(pageable);
    }

    public void partialUpdateChallenge(Long challengeId, UpdateChallengeCommand command) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));
        String newTitle = command.title() != null ? command.title() : challenge.getTitle();
        String newDescription = command.description() != null ? command.description() : challenge.getDescription();
        PeriodType newPeriodType = command.periodType() != null ? command.periodType() : challenge.getPeriodType();
        challenge.update(newTitle, newDescription, newPeriodType);
        challengeRepository.save(challenge);
    }
}
