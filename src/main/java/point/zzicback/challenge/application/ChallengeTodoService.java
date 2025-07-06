package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.dto.result.ChallengeTodoResult;
import point.zzicback.challenge.application.mapper.ChallengeTodoMapper;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.*;
import point.zzicback.common.error.*;
import point.zzicback.experience.application.event.ChallengeTodoCompletedEvent;
import point.zzicback.member.domain.Member;
import point.zzicback.challenge.infrastructure.ChallengeRepository;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeTodoService {
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeTodoMapper challengeTodoMapper;
    private final ApplicationEventPublisher eventPublisher;

    public void completeChallenge(Long challengeId, Member member, LocalDate targetDate) {
        // 챌린지 확인
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new NotFoundException("챌린지를 찾을 수 없습니다"));
        
        // 참여 상태 확인
        ChallengeParticipation participation = participationRepository
            .findByMemberAndChallenge_IdAndJoinOutIsNull(member, challengeId)
            .orElseThrow(() -> new ForbiddenException("참여하지 않은 챌린지입니다"));
        
        // 챌린지 활성 상태 확인
        if (!challenge.isActive()) {
            throw new BusinessException("BIZ_001", "종료되었거나 시작되지 않은 챌린지입니다");
        }
        
        // 기존 투두 확인
        Optional<ChallengeTodo> existingTodo = challengeTodoRepository
            .findByChallengeParticipationAndTargetDate(participation, targetDate);
        
        if (existingTodo.isPresent() && existingTodo.get().isCompleted()) {
            throw new ConflictException("이미 완료한 챌린지 투두입니다");
        }
        
        // 투두 생성 또는 업데이트
        ChallengeTodo todo = existingTodo.orElseGet(() -> {
            Period period = calculatePeriod(challenge.getPeriodType(), targetDate);
            return ChallengeTodo.builder()
                .challengeParticipation(participation)
                .period(period)
                .targetDate(targetDate)
                .build();
        });
        
        todo.complete(targetDate);
        challengeTodoRepository.save(todo);
        
        // 챌린지 투두 완료 이벤트 발생
        eventPublisher.publishEvent(new ChallengeTodoCompletedEvent(
            member.getId(),
            challenge.getId(),
            challenge.getTitle()
        ));
    }
    
    public void cancelCompleteChallenge(Long todoId, Member member) {
        ChallengeTodo todo = challengeTodoRepository.findById(todoId)
            .orElseThrow(() -> new NotFoundException("챌린지 투두를 찾을 수 없습니다"));
        
        // 권한 확인
        if (!todo.getChallengeParticipation().getMember().getId().equals(member.getId())) {
            throw new ForbiddenException("다른 사용자의 챌린지 투두를 취소할 수 없습니다");
        }
        
        todo.cancel();
        challengeTodoRepository.save(todo);
    }
    
    @Transactional(readOnly = true)
    public Page<ChallengeTodoResult> getAllChallengeTodos(Member member, Pageable pageable) {
        return challengeTodoRepository.findAllByMember(member, pageable)
            .map(challengeTodoMapper::toResult);
    }
    
    @Transactional(readOnly = true)
    public ChallengeTodoResult getChallengeTodoByChallenge(Long challengeId, Member member, LocalDate date) {
        ChallengeTodo todo = challengeTodoRepository
            .findByChallengeMemberAndDate(challengeId, member, date)
            .orElseThrow(() -> new NotFoundException("챌린지 투두를 찾을 수 없습니다"));
        
        return challengeTodoMapper.toResult(todo);
    }
    
    private Period calculatePeriod(PeriodType periodType, LocalDate targetDate) {
        return switch (periodType) {
            case DAILY -> new Period(targetDate, targetDate);
            case WEEKLY -> {
                LocalDate startOfWeek = targetDate.minusDays(targetDate.getDayOfWeek().getValue() - 1);
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                yield new Period(startOfWeek, endOfWeek);
            }
            case MONTHLY -> {
                LocalDate startOfMonth = targetDate.withDayOfMonth(1);
                LocalDate endOfMonth = targetDate.withDayOfMonth(targetDate.lengthOfMonth());
                yield new Period(startOfMonth, endOfMonth);
            }
        };
    }
}