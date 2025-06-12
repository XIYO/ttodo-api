package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.result.ChallengeDto;
import point.zzicback.challenge.application.dto.result.ChallengeJoinedDto;
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
    public Page<ChallengeDto> getChallengesWithParticipation(Member member, Pageable pageable) {
        Page<Challenge> challengePage = challengeRepository.findAll(pageable);
        List<Long> participatedChallengeIds = challengeParticipationRepository.findByMemberAndJoinOutIsNull(member)
                .stream()
                .map(participation -> participation.getChallenge().getId())
                .toList();
        
        return challengePage.map(challenge -> new ChallengeDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                participatedChallengeIds.contains(challenge.getId())
        ));
    }

    @Transactional(readOnly = true)
    public Page<ChallengeDto> searchChallengesWithParticipation(Member member, String keyword, String sort, Pageable pageable) {
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
        
        return challengePage.map(challenge -> new ChallengeDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                participatedChallengeIds.contains(challenge.getId())
        ));
    }

    @Transactional(readOnly = true)
    public Page<ChallengeDto> searchChallengesWithFilter(Member member, String keyword, String sort, Boolean join, Pageable pageable) {
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
        
        List<ChallengeDto> filteredChallenges = challengePage.getContent().stream()
                .map(challenge -> {
                    boolean isParticipated = participatedChallengeIds.contains(challenge.getId());
                    return new ChallengeDto(
                            challenge.getId(),
                            challenge.getTitle(),
                            challenge.getDescription(),
                            challenge.getStartDate(),
                            challenge.getEndDate(),
                            challenge.getPeriodType(),
                            isParticipated
                    );
                })
                .filter(challengeDto -> join == null || join.equals(challengeDto.participationStatus()))
                .toList();
        
        return new PageImpl<>(filteredChallenges, pageable, filteredChallenges.size());
    }

    @Transactional(readOnly = true)
    public List<ChallengeJoinedDto> getChallengesByMember(Member member) {
        List<Challenge> allChallenges = challengeRepository.findAll();
        List<Long> participatedChallengeIds = challengeParticipationRepository.findByMemberAndJoinOutIsNull(member)
                .stream()
                .map(participation -> participation.getChallenge().getId())
                .toList();
        
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
    public Page<ChallengeJoinedDto> getChallengesByMember(Member member, Pageable pageable) {
        Page<Challenge> challengePage = challengeRepository.findAll(pageable);
        List<Long> participatedChallengeIds = challengeParticipationRepository.findByMemberAndJoinOutIsNull(member)
                .stream()
                .map(participation -> participation.getChallenge().getId())
                .toList();
        
        return challengePage.map(challenge -> new ChallengeJoinedDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                participatedChallengeIds.contains(challenge.getId())
        ));
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
