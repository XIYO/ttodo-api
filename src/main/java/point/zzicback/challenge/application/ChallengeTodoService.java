package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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
        var existingTodo = (cp.getChallenge().getPeriodType() == PeriodType.DAILY)
                ? challengeTodoRepository.findByChallengeParticipationAndTargetDate(cp, currentDate)
                : challengeTodoRepository.findByChallengeParticipation(cp);
        
        if (existingTodo.isPresent()) {
            ChallengeTodo todo = existingTodo.get();
            if (!todo.isCompleted()) {
                todo.complete(currentDate);
                challengeTodoRepository.save(todo);
            }
        } else {
            LocalDate targetDate = (cp.getChallenge().getPeriodType() == PeriodType.DAILY) 
                    ? currentDate 
                    : calculateTargetDate(cp.getChallenge().getPeriodType());
                    
            ChallengeTodo newTodo = ChallengeTodo.builder()
                    .challengeParticipation(cp)
                    .targetDate(targetDate)
                    .build();
            newTodo.complete(currentDate);
            challengeTodoRepository.save(newTodo);
        }
    }

    public void cancelCompleteChallenge(Long challengeId, Member member, LocalDate currentDate) {
        challengeService.findById(challengeId);

        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지에 참여하지 않았습니다."));

        ChallengeTodo challengeTodo;
        if (participation.getChallenge().getPeriodType() == PeriodType.DAILY) {
            challengeTodo = challengeTodoRepository
                    .findByChallengeParticipationAndTargetDate(participation, currentDate)
                    .orElseThrow(() -> new IllegalArgumentException("챌린지 Todo를 찾을 수 없습니다."));
        } else {
            challengeTodo = challengeTodoRepository
                    .findByChallengeParticipation(participation)
                    .orElseThrow(() -> new IllegalArgumentException("챌린지 Todo를 찾을 수 없습니다."));
        }

        challengeTodoRepository.delete(challengeTodo);
    }

    public boolean isCompletedInPeriod(ChallengeParticipation cp, LocalDate date) {
        var todoOptional = (cp.getChallenge().getPeriodType() == PeriodType.DAILY)
                ? challengeTodoRepository.findByChallengeParticipationAndTargetDate(cp, date)
                : challengeTodoRepository.findByChallengeParticipation(cp);
                
        return todoOptional
                .map(todo -> todo.isInPeriod(cp.getChallenge().getPeriodType(), date) && todo.isCompleted())
                .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public List<ChallengeTodoDto> getAllChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberAndJoinOutIsNull(member);

        return participations.stream()
                .flatMap(this::createChallengeTodoStream)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChallengeTodoDto> getAllChallengeTodos(Member member, Pageable pageable) {
        List<ChallengeTodoDto> allTodos = getAllChallengeTodos(member);
        allTodos = applySorting(allTodos, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTodos.size());
        List<ChallengeTodoDto> pagedTodos = allTodos.subList(start, end);
        return new PageImpl<>(pagedTodos, pageable, allTodos.size());
    }
 
    @Transactional(readOnly = true)
    public List<ChallengeTodoDto> getUncompletedChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberAndJoinOutIsNull(member);

        return participations.stream()
                .flatMap(this::createUncompletedChallengeTodoStream)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChallengeTodoDto> getUncompletedChallengeTodos(Member member, Pageable pageable) {
        List<ChallengeTodoDto> allTodos = getUncompletedChallengeTodos(member);
        allTodos = applySorting(allTodos, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTodos.size());
        List<ChallengeTodoDto> pagedTodos = allTodos.subList(start, end);
        return new PageImpl<>(pagedTodos, pageable, allTodos.size());
    }

    @Transactional(readOnly = true)
    public List<ChallengeTodoDto> getCompletedChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberAndJoinOutIsNull(member);

        return participations.stream()
                .flatMap(this::createCompletedChallengeTodoStream)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChallengeTodoDto> getCompletedChallengeTodos(Member member, Pageable pageable) {
        List<ChallengeTodoDto> allTodos = getCompletedChallengeTodos(member);
        allTodos = applySorting(allTodos, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTodos.size());
        List<ChallengeTodoDto> pagedTodos = allTodos.subList(start, end);
        return new PageImpl<>(pagedTodos, pageable, allTodos.size());
    }

    private Stream<ChallengeTodoDto> createChallengeTodoStream(ChallengeParticipation participation) {
        try {
            var virtualTodo = createVirtualChallengeTodo(participation);
            LocalDate currentDate = LocalDate.now();
            PeriodType periodType = participation.getChallenge().getPeriodType();
            
            if (!isWithinChallengeRange(participation.getChallenge(), currentDate)) {
                return Stream.empty();
            }
            
            if (!virtualTodo.isInPeriod(periodType, currentDate)) {
                return Stream.empty();
            }
            
            var existingTodo = (periodType == PeriodType.DAILY) 
                ? challengeTodoRepository.findByChallengeParticipationAndTargetDate(participation, currentDate)
                : challengeTodoRepository.findByChallengeParticipation(participation);
                
            if (existingTodo.isPresent()) {
                ChallengeTodo actualTodo = existingTodo.get();
                if (!actualTodo.isInPeriod(periodType, currentDate)) {
                    return Stream.empty();
                }
                return Stream.of(ChallengeTodoDto.from(actualTodo));
            } else {
                return Stream.of(ChallengeTodoDto.from(virtualTodo));
            }
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    private Stream<ChallengeTodoDto> createUncompletedChallengeTodoStream(ChallengeParticipation participation) {
        try {
            var virtualTodo = createVirtualChallengeTodo(participation);
            LocalDate currentDate = LocalDate.now();
            PeriodType periodType = participation.getChallenge().getPeriodType();
            
            if (!isWithinChallengeRange(participation.getChallenge(), currentDate)) {
                return Stream.empty();
            }
            
            if (!virtualTodo.isInPeriod(periodType, currentDate)) {
                return Stream.empty();
            }
            
            var existingTodo = (periodType == PeriodType.DAILY) 
                ? challengeTodoRepository.findByChallengeParticipationAndTargetDate(participation, currentDate)
                : challengeTodoRepository.findByChallengeParticipation(participation);

            if (existingTodo.isPresent()) {
                ChallengeTodo todo = existingTodo.get();
                if (!todo.isInPeriod(periodType, currentDate)) {
                    return Stream.empty();
                }
                if (!todo.isCompleted()) {
                    return Stream.of(ChallengeTodoDto.from(todo));
                } else {
                    return Stream.empty();
                }
            } else {
                return Stream.of(ChallengeTodoDto.from(virtualTodo));
            }
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    private Stream<ChallengeTodoDto> createCompletedChallengeTodoStream(ChallengeParticipation participation) {
        try {
            var virtualTodo = createVirtualChallengeTodo(participation);
            LocalDate currentDate = LocalDate.now();
            
            if (!isWithinChallengeRange(participation.getChallenge(), currentDate)) {
                return Stream.empty();
            }
            
            if (!virtualTodo.isInPeriod(participation.getChallenge().getPeriodType(), currentDate)) {
                return Stream.empty();
            }
            
            return challengeTodoRepository.findByChallengeParticipation(participation)
                    .filter(ChallengeTodo::isCompleted)
                    .map(ChallengeTodoDto::from)
                    .map(Stream::of)
                    .orElse(Stream.empty());
        } catch (Exception e) {
            return Stream.empty();
        }
    }

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

    private LocalDate calculateTargetDate(PeriodType periodType) {
        LocalDate today = LocalDate.now();
        return switch (periodType) {
            case DAILY -> today;
            case WEEKLY -> today;
            case MONTHLY -> today;
        };
    }

    public void completeChallenge(Long challengeId, Member member, LocalDate currentDate) {
        challengeService.findById(challengeId);

        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지에 참여하지 않았습니다."));
        
        completeChallenge(participation, currentDate);
    }

    private List<ChallengeTodoDto> applySorting(List<ChallengeTodoDto> todos, Sort sort) {
        if (sort.isEmpty()) return todos;
        
        Comparator<ChallengeTodoDto> finalComparator = null;
        
        for (Sort.Order order : sort) {
            Comparator<ChallengeTodoDto> currentComparator = getComparatorByProperty(order.getProperty());
            
            if (order.isDescending()) {
                currentComparator = currentComparator.reversed();
            }
            
            finalComparator = (finalComparator == null) 
                ? currentComparator 
                : finalComparator.thenComparing(currentComparator);
        }
        
        return todos.stream()
                .sorted(finalComparator != null ? finalComparator : Comparator.comparing(ChallengeTodoDto::id))
                .toList();
    }

    private Comparator<ChallengeTodoDto> getComparatorByProperty(String property) {
        return switch (property) {
            case "challengeTitle" -> Comparator.comparing(ChallengeTodoDto::challengeTitle);
            case "startDate" -> Comparator.comparing(ChallengeTodoDto::startDate);  
            case "endDate" -> Comparator.comparing(ChallengeTodoDto::endDate);
            case "periodType" -> Comparator.comparing(ChallengeTodoDto::periodType);
            default -> Comparator.comparing(ChallengeTodoDto::id);
        };
    }

    private boolean isWithinChallengeRange(Challenge challenge, LocalDate date) {
        return !date.isBefore(challenge.getStartDate()) && !date.isAfter(challenge.getEndDate());
    }
}
