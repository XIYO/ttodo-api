package point.zzicback.challenge.application;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.dto.result.ChallengeTodoResult;
import point.zzicback.challenge.application.mapper.ChallengeTodoMapper;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.*;
import point.zzicback.common.error.*;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeTodoService {
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeService challengeService;
    private final ChallengeTodoMapper challengeTodoMapper;
    
    @PersistenceContext
    private EntityManager entityManager;

    public void completeChallenge(ChallengeParticipation cp, LocalDate currentDate) {
        var existingTodo = (cp.getChallenge().getPeriodType() == PeriodType.DAILY)
                ? challengeTodoRepository.findByChallengeParticipationAndTargetDate(cp, currentDate)
                : challengeTodoRepository.findByChallengeParticipation(cp);
        
        if (existingTodo.isPresent()) {
            ChallengeTodo todo = existingTodo.get();
            if (todo.isCompleted()) {
                throw new BusinessException("이미 완료된 챌린지입니다.");
            }
            todo.complete(currentDate);
            challengeTodoRepository.save(todo);
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

    @Transactional
    public void cancelCompleteChallenge(Long challengeId, Member member, LocalDate currentDate) {
        challengeService.findById(challengeId);

        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)
                .orElseThrow(() -> new BusinessException("해당 챌린지를 완료하지 않았습니다."));

        ChallengeTodo challengeTodo;
        if (participation.getChallenge().getPeriodType() == PeriodType.DAILY) {
            challengeTodo = challengeTodoRepository
                    .findByChallengeParticipationAndTargetDate(participation, currentDate)
                    .orElseThrow(() -> new EntityNotFoundException("ChallengeTodo", "participation-" + participation.getId() + "-date-" + currentDate));
        } else {
            challengeTodo = challengeTodoRepository
                    .findByChallengeParticipation(participation)
                    .orElseThrow(() -> new EntityNotFoundException("ChallengeTodo", "participation-" + participation.getId()));
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
    public List<ChallengeTodoResult> getAllChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberAndJoinOutIsNull(member);

        return participations.stream()
                .flatMap(this::createChallengeTodoStream)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChallengeTodoResult> getAllChallengeTodos(Member member, Pageable pageable) {
        List<ChallengeTodoResult> allTodos = getAllChallengeTodos(member);
        allTodos = applySorting(allTodos, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTodos.size());
        List<ChallengeTodoResult> pagedTodos = allTodos.subList(start, end);
        return new PageImpl<>(pagedTodos, pageable, allTodos.size());
    }
 
    @Transactional(readOnly = true)
    public List<ChallengeTodoResult> getUncompletedChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberAndJoinOutIsNull(member);

        return participations.stream()
                .flatMap(this::createUncompletedChallengeTodoStream)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChallengeTodoResult> getUncompletedChallengeTodos(Member member, Pageable pageable) {
        List<ChallengeTodoResult> allTodos = getUncompletedChallengeTodos(member);
        allTodos = applySorting(allTodos, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTodos.size());
        List<ChallengeTodoResult> pagedTodos = allTodos.subList(start, end);
        return new PageImpl<>(pagedTodos, pageable, allTodos.size());
    }

    @Transactional(readOnly = true)
    public List<ChallengeTodoResult> getCompletedChallengeTodos(Member member) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberAndJoinOutIsNull(member);

        return participations.stream()
                .flatMap(this::createCompletedChallengeTodoStream)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChallengeTodoResult> getCompletedChallengeTodos(Member member, Pageable pageable) {
        List<ChallengeTodoResult> allTodos = getCompletedChallengeTodos(member);
        allTodos = applySorting(allTodos, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTodos.size());
        List<ChallengeTodoResult> pagedTodos = allTodos.subList(start, end);
        return new PageImpl<>(pagedTodos, pageable, allTodos.size());
    }

    private Stream<ChallengeTodoResult> createChallengeTodoStream(ChallengeParticipation participation) {
        LocalDate currentDate = LocalDate.now();
        var virtualTodo = createVirtualChallengeTodo(participation, currentDate);
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
            return Stream.of(challengeTodoMapper.toResult(todo));
        } else {
            return Stream.of(challengeTodoMapper.toResult(virtualTodo));
        }
    }

    private Stream<ChallengeTodoResult> createUncompletedChallengeTodoStream(ChallengeParticipation participation) {
        try {
            LocalDate currentDate = LocalDate.now();
            var virtualTodo = createVirtualChallengeTodo(participation, currentDate);
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
                    return Stream.of(challengeTodoMapper.toResult(todo));
                } else {
                    return Stream.empty();
                }
            } else {
                return Stream.of(challengeTodoMapper.toResult(virtualTodo));
            }
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    private Stream<ChallengeTodoResult> createCompletedChallengeTodoStream(ChallengeParticipation participation) {
        try {
            LocalDate currentDate = LocalDate.now();
            var virtualTodo = createVirtualChallengeTodo(participation, currentDate);
            
            if (!isWithinChallengeRange(participation.getChallenge(), currentDate)) {
                return Stream.empty();
            }
            
            if (!virtualTodo.isInPeriod(participation.getChallenge().getPeriodType(), currentDate)) {
                return Stream.empty();
            }
            
        return challengeTodoRepository.findByChallengeParticipation(participation)
                    .filter(ChallengeTodo::isCompleted)
                    .map(challengeTodoMapper::toResult)
                    .map(Stream::of)
                    .orElse(Stream.empty());
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    ChallengeTodo createVirtualChallengeTodo(ChallengeParticipation participation, LocalDate currentDate) {
        if (participation == null) {
            throw new BusinessException("ChallengeParticipation cannot be null");
        }
        
        Challenge challenge = participation.getChallenge();
        if (challenge == null) {
            throw new BusinessException("Challenge cannot be null");
        }
        
        LocalDate targetDate = currentDate;

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
                .orElseThrow(() -> new BusinessException("해당 챌린지에 참여하지 않았습니다."));
        
        completeChallenge(participation, currentDate);
    }

    private List<ChallengeTodoResult> applySorting(List<ChallengeTodoResult> todos, Sort sort) {
        if (sort.isEmpty()) return todos;
        
        Comparator<ChallengeTodoResult> finalComparator = null;
        
        for (Sort.Order order : sort) {
            Comparator<ChallengeTodoResult> currentComparator = getComparatorByProperty(order.getProperty());
            
            if (order.isDescending()) {
                currentComparator = currentComparator.reversed();
            }
            
            finalComparator = (finalComparator == null) 
                ? currentComparator 
                : finalComparator.thenComparing(currentComparator);
        }
        
        return todos.stream()
                .sorted(finalComparator != null ? finalComparator : Comparator.comparing(ChallengeTodoResult::id, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private Comparator<ChallengeTodoResult> getComparatorByProperty(String property) {
        return switch (property) {
            case "challengeTitle" -> Comparator.comparing(ChallengeTodoResult::challengeTitle, Comparator.nullsLast(String::compareTo));
            case "startDate" -> Comparator.comparing(ChallengeTodoResult::startDate, Comparator.nullsLast(Comparator.naturalOrder()));  
            case "endDate" -> Comparator.comparing(ChallengeTodoResult::endDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "periodType" -> Comparator.comparing(ChallengeTodoResult::periodType, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(ChallengeTodoResult::id, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private boolean isWithinChallengeRange(Challenge challenge, LocalDate date) {
        return !date.isBefore(challenge.getStartDate()) && !date.isAfter(challenge.getEndDate());
    }

    @Transactional(readOnly = true)
    public double calculateSuccessRate(Long challengeId) {
        long completedParticipantCount = challengeTodoRepository.countCompletedParticipantsByChallengeId(challengeId);
        return completedParticipantCount;
    }
}
