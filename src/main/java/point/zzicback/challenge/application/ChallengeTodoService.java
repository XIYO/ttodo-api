package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.ChallengeParticipationRepository;
import point.zzicback.challenge.infrastructure.ChallengeTodoRepository;
import point.zzicback.challenge.application.dto.result.ChallengeTodoDto;
import point.zzicback.member.domain.Member;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeTodoService {
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeService challengeService;
    
    @PersistenceContext
    private EntityManager entityManager;

    public void completeChallenge(ChallengeParticipation cp, LocalDate currentDate) {
        ChallengeTodo challengeTodo = challengeTodoRepository
                .findByChallengeParticipation(cp)
                .orElseGet(() -> {
                    LocalDate targetDate = calculateTargetDate(cp.getChallenge().getPeriodType());
                    ChallengeTodo newTodo = ChallengeTodo.builder()
                            .challengeParticipation(cp)
                            .targetDate(targetDate)
                            .build();
                    return challengeTodoRepository.save(newTodo);
                });

        if (!challengeTodo.isCompleted()) {
            challengeTodo.complete(currentDate);
            challengeTodoRepository.save(challengeTodo);
        }
    }

    public void cancelCompleteChallenge(Long challengeId, Member member, LocalDate currentDate) {
        challengeService.findById(challengeId);

        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_Id(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지에 참여하지 않았습니다."));

        ChallengeTodo challengeTodo = challengeTodoRepository
                .findByChallengeParticipation(participation)
                .orElseThrow(() -> new IllegalArgumentException("챌린지 Todo를 찾을 수 없습니다."));

        challengeTodo.cancel(currentDate);
        challengeTodoRepository.delete(challengeTodo);
    }

    public boolean isCompletedInPeriod(ChallengeParticipation cp, LocalDate date) {
        return challengeTodoRepository
                .findByChallengeParticipation(cp)
                .map(todo -> todo.isInPeriod(cp.getChallenge().getPeriodType(), date) && todo.isCompleted())
                .orElse(false);
    }
    
    // 챌린지 참여자가 해야 할 챌린지 투두를 모두 조회
    @Transactional(readOnly = true)
    public List<ChallengeTodoDto> getAllChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMember(member);

        return participations.stream()
                .flatMap(this::createChallengeTodoStream)
                .toList();
    }
 
    // 챌린지 참여자가 완료되지 않은 챌린지 투두를 모두 조회
    @Transactional(readOnly = true)
    public List<ChallengeTodoDto> getUncompletedChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMember(member);

        return participations.stream()
                .flatMap(this::createUncompletedChallengeTodoStream)
                .toList();
    }

    // 챌린지 참여자가 완료된 챌린지 투두를 모두 조회
    @Transactional(readOnly = true)
    public List<ChallengeTodoDto> getCompletedChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMember(member);

        return participations.stream()
                .flatMap(this::createCompletedChallengeTodoStream)
                .toList();
    }

    private Stream<ChallengeTodoDto> createChallengeTodoStream(ChallengeParticipation participation) {
        try {
            return challengeTodoRepository.findByChallengeParticipation(participation)
                    .map(ChallengeTodoDto::from)
                    .map(Stream::of)
                    .orElseGet(() -> Stream.of(ChallengeTodoDto.from(createVirtualChallengeTodo(participation))));
        } catch (Exception e) {
            // 에러 로깅 및 기본값 반환으로 안정성 확보
            System.err.println("Error creating ChallengeTodoStream for participation: " + participation.getId() + ", Error: " + e.getMessage());
            return Stream.empty();
        }
    }

    private Stream<ChallengeTodoDto> createUncompletedChallengeTodoStream(ChallengeParticipation participation) {
        try {
            var existingTodo = challengeTodoRepository.findByChallengeParticipation(participation);

            if (existingTodo.isPresent()) {
                // 기존 Todo가 있지만 완료되지 않은 경우에만 반환
                ChallengeTodo todo = existingTodo.get();
                if (!todo.isCompleted()) {
                    return Stream.of(ChallengeTodoDto.from(todo));
                } else {
                    return Stream.empty();
                }
            } else {
                // DB에 Todo가 없다는 것은 아직 성공하지 않았다는 의미이므로 가상 Todo 생성
                var virtualTodo = createVirtualChallengeTodo(participation);
                return Stream.of(ChallengeTodoDto.from(virtualTodo));
            }
        } catch (Exception e) {
            // 에러 로깅 및 기본값 반환으로 안정성 확보
            System.err.println("Error creating UncompletedChallengeTodoStream for participation: " + participation.getId() + ", Error: " + e.getMessage());
            return Stream.empty();
        }
    }

    private Stream<ChallengeTodoDto> createCompletedChallengeTodoStream(ChallengeParticipation participation) {
        try {
            return challengeTodoRepository.findByChallengeParticipation(participation)
                    .filter(ChallengeTodo::isCompleted)
                    .map(ChallengeTodoDto::from)
                    .map(Stream::of)
                    .orElse(Stream.empty());
        } catch (Exception e) {
            System.err.println("Error creating CompletedChallengeTodoStream for participation: " + participation.getId() + ", Error: " + e.getMessage());
            return Stream.empty();
        }
    }

    // 가상 챌린지 Todo 생성
    ChallengeTodo createVirtualChallengeTodo(ChallengeParticipation participation) {
        if (participation == null) {
            throw new IllegalArgumentException("ChallengeParticipation cannot be null");
        }
        
        Challenge challenge = participation.getChallenge();
        if (challenge == null) {
            throw new IllegalArgumentException("Challenge cannot be null");
        }
        
        LocalDate targetDate = calculateTargetDate(challenge.getPeriodType());

        return ChallengeTodo.builder()
                .challengeParticipation(participation)
                .targetDate(targetDate)
                .build();
    }

    // 챌린지의 주기 유형에 따라 목표 날짜를 계산
    private LocalDate calculateTargetDate(PeriodType periodType) {
        LocalDate today = LocalDate.now();
        return switch (periodType) {
            case DAILY -> today;
            case WEEKLY -> {
                LocalDate monday = today.with(previousOrSame(java.time.DayOfWeek.MONDAY));
                yield monday;
            }
            case MONTHLY -> today.withDayOfMonth(1);
        };
    }

    public void completeChallenge(Long challengeId, Member member, LocalDate currentDate) {
        challengeService.findById(challengeId);

        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_Id(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지에 참여하지 않았습니다."));
        
        completeChallenge(participation, currentDate);
    }
}
