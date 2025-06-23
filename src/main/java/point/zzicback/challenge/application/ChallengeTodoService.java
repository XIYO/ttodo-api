package point.zzicback.challenge.application;

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

    public void completeChallenge(ChallengeParticipation cp, LocalDate currentDate) {
        Optional<ChallengeTodo> existingTodo = (cp.getChallenge().getPeriodType() == PeriodType.DAILY)
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
            LocalDate targetDate = cp.getChallenge().getPeriodType().calculateTargetDate(currentDate);
                    
            ChallengeTodo newTodo = ChallengeTodo.builder()
                    .challengeParticipation(cp)
                    .targetDate(targetDate)
                    .build();
            newTodo.complete(currentDate);
            challengeTodoRepository.save(newTodo);
        }
    }

    @Transactional
    public void cancelCompleteChallenge(Long todoId, Member member) {
        ChallengeTodo challengeTodo = challengeTodoRepository.findById(todoId)
                .orElseThrow(() -> new EntityNotFoundException("ChallengeTodo", todoId));
        
        if (!challengeTodo.getChallengeParticipation().getMember().equals(member)) {
            throw new BusinessException("해당 투두에 대한 권한이 없습니다.");
        }
        
        challengeTodoRepository.delete(challengeTodo);
    }
    
    @Transactional(readOnly = true)
    public Page<ChallengeTodoResult> getAllChallengeTodos(Member member, Pageable pageable) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberAndJoinOutIsNull(member);
        List<ChallengeTodoResult> allTodos = participations.stream()
                .flatMap(this::createChallengeTodoStream)
                .toList();
        allTodos = applySorting(allTodos, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTodos.size());
        List<ChallengeTodoResult> pagedTodos = allTodos.subList(start, end);
        return new PageImpl<>(pagedTodos, pageable, allTodos.size());
    }

    private Stream<ChallengeTodoResult> createChallengeTodoStream(ChallengeParticipation participation) {
        LocalDate currentDate = LocalDate.now();
        ChallengeTodo virtualTodo = createVirtualChallengeTodo(participation, currentDate);
        PeriodType periodType = participation.getChallenge().getPeriodType();
        
        if (!isWithinChallengeRange(participation.getChallenge(), currentDate)) {
            return Stream.empty();
        }
        
        if (!virtualTodo.isInPeriod(periodType, currentDate)) {
            return Stream.empty();
        }
        
        Optional<ChallengeTodo> existingTodo = (periodType == PeriodType.DAILY) 
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

    public void completeChallenge(Long challengeId, Member member, LocalDate currentDate) {
        challengeService.findById(challengeId);

        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)
                .orElseThrow(() -> new BusinessException("해당 챌린지에 참여하지 않았습니다."));
        
        completeChallenge(participation, currentDate);
    }

    @Transactional(readOnly = true)
    public ChallengeTodoResult getChallengeTodoByChallenge(Long challengeId, Member member, LocalDate currentDate) {
        challengeService.findById(challengeId);

        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)
                .orElseThrow(() -> new BusinessException("해당 챌린지에 참여하지 않았습니다."));

        // 기존 완료된 투두를 찾아서 반환
        Optional<ChallengeTodo> existingTodo = (participation.getChallenge().getPeriodType() == PeriodType.DAILY)
                ? challengeTodoRepository.findByChallengeParticipationAndTargetDate(participation, currentDate)
                : challengeTodoRepository.findByChallengeParticipation(participation);

        if (existingTodo.isPresent()) {
            return challengeTodoMapper.toResult(existingTodo.get());
        } else {
            // 가상 투두 생성 및 반환 (완료되지 않은 상태)
            ChallengeTodo virtualTodo = createVirtualChallengeTodo(participation, currentDate);
            return challengeTodoMapper.toResult(virtualTodo);
        }
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
}
