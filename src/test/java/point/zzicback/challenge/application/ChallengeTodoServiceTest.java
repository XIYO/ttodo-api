package point.zzicback.challenge.application;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.Import;
import point.zzicback.challenge.application.dto.result.ChallengeTodoDto;
import point.zzicback.challenge.application.mapper.ChallengeApplicationMapperImpl;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.*;
import point.zzicback.common.error.*;
import point.zzicback.member.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    ChallengeTodoService.class,
    ChallengeService.class,
    ChallengeApplicationMapperImpl.class
})
class ChallengeTodoServiceTest {

    @Autowired
    private ChallengeTodoService challengeTodoService;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeParticipationRepository participationRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member testMember;
    private List<ChallengeParticipation> allParticipations;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .build();
        memberRepository.save(testMember);
        
        allParticipations = List.of(
            createChallengeWithParticipation(PeriodType.DAILY, "일간 챌린지"),
            createChallengeWithParticipation(PeriodType.WEEKLY, "주간 챌린지"),
            createChallengeWithParticipation(PeriodType.MONTHLY, "월간 챌린지")
        );
    }

    private ChallengeParticipation createChallengeWithParticipation(PeriodType periodType, String title) {
        var challenge = Challenge.builder()
                .title(title)
                .description(title + " 설명")
                .periodType(periodType)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(challenge);

        var participation = ChallengeParticipation.builder()
                .member(testMember)
                .challenge(challenge)
                .build();
        return participationRepository.save(participation);
    }

    private void completeChallengeTodo(ChallengeParticipation participation) {
        challengeTodoService.completeChallenge(participation, LocalDate.now());
        entityManager.flush();
    }

    @Test
    @DisplayName("챌린지 완료 처리")
    void completeChallenge() {
        var participation = allParticipations.get(0);
        
        challengeTodoService.completeChallenge(participation, LocalDate.now());

        var todos = challengeTodoRepository.findAll();
        assertThat(todos).hasSize(1);
        
        var completedTodo = todos.get(0);
        assertThat(completedTodo.getChallengeParticipation().getId()).isEqualTo(participation.getId());
        assertThat(completedTodo.getDone()).isTrue();
    }

    @Test
    @DisplayName("챌린지 완료 취소")
    void cancelCompleteChallenge() {
        var participation = allParticipations.get(0);
        completeChallengeTodo(participation);
        
        challengeTodoService.cancelCompleteChallenge(participation.getChallenge().getId(), testMember, LocalDate.now());

        var todos = challengeTodoRepository.findAll();
        assertThat(todos).isEmpty();
        
        // entityManager.clear() 후 member를 다시 조회
        var refreshedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        
        // Virtual todo 확인 - done이 false여야 함
        var allTodos = challengeTodoService.getAllChallengeTodos(refreshedMember);
        
        // 디버깅을 위해 모든 todo 정보 출력
        assertThat(allTodos).hasSize(3); // 3개 챌린지 모두 virtual todo가 나와야 함
        
        var canceledChallengeTodo = allTodos.stream()
                .filter(dto -> dto.challengeTitle().equals(participation.getChallenge().getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected virtual todo not found for challenge: " + participation.getChallenge().getTitle()));
        
        assertThat(canceledChallengeTodo.done()).isFalse();
        assertThat(canceledChallengeTodo.isPersisted()).isFalse();
    }

    @Test
    @DisplayName("미완료 투두만 있는 경우 - DAILY, WEEKLY, MONTHLY")
    void getTodos_OnlyIncomplete() {
        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).hasSize(3);
        assertThat(allTodos.stream().allMatch(dto -> !dto.done())).isTrue();
        assertThat(allTodos.stream().allMatch(dto -> !dto.isPersisted())).isTrue();

        var periodTypes = allTodos.stream().map(ChallengeTodoDto::periodType).toList();
        assertThat(periodTypes).containsExactlyInAnyOrder(PeriodType.DAILY, PeriodType.WEEKLY, PeriodType.MONTHLY);
    }

    @Test
    @DisplayName("완료된 투두만 있는 경우 - DAILY, WEEKLY, MONTHLY")
    void getTodos_OnlyCompleted() {
        allParticipations.forEach(this::completeChallengeTodo);

        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).isEmpty();
        assertThat(allTodos.stream().allMatch(ChallengeTodoDto::done)).isTrue();
        assertThat(allTodos.stream().allMatch(ChallengeTodoDto::isPersisted)).isTrue();

        var periodTypes = allTodos.stream().map(ChallengeTodoDto::periodType).toList();
        assertThat(periodTypes).containsExactlyInAnyOrder(PeriodType.DAILY, PeriodType.WEEKLY, PeriodType.MONTHLY);
    }

    @Test
    @DisplayName("완료/미완료 투두 혼재 - DAILY, WEEKLY, MONTHLY")
    void getTodos_Mixed() {
        completeChallengeTodo(allParticipations.get(0));

        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).hasSize(2);

        var completedTodos = allTodos.stream().filter(ChallengeTodoDto::done).toList();
        var incompleteTodos = allTodos.stream().filter(dto -> !dto.done()).toList();

        assertThat(completedTodos).hasSize(1);
        assertThat(incompleteTodos).hasSize(2);
        assertThat(completedTodos.get(0).periodType()).isEqualTo(PeriodType.DAILY);
        assertThat(completedTodos.get(0).isPersisted()).isTrue();

        var incompletePeriodTypes = incompleteTodos.stream().map(ChallengeTodoDto::periodType).toList();
        assertThat(incompletePeriodTypes).containsExactlyInAnyOrder(PeriodType.WEEKLY, PeriodType.MONTHLY);
        assertThat(incompleteTodos.stream().allMatch(dto -> !dto.isPersisted())).isTrue();
    }

    @Test
    @DisplayName("예외 처리 - 참여하지 않은 챌린지")
    void cancelCompleteChallenge_NotParticipating() {
        var anotherMember = Member.builder()
                .email("another@test.com")
                .password("password")
                .nickname("another")
                .build();
        memberRepository.save(anotherMember);

        var participation = allParticipations.get(0);

        assertThatThrownBy(() -> 
            challengeTodoService.cancelCompleteChallenge(participation.getChallenge().getId(), anotherMember, LocalDate.now())
        )
        .isInstanceOf(BusinessException.class)
        .hasMessage("해당 챌린지를 완료하지 않았습니다.");
    }

    @Test
    @DisplayName("예외 처리 - Todo 찾을 수 없음")
    void cancelCompleteChallenge_TodoNotFound() {
        var participation = allParticipations.get(0);

        assertThatThrownBy(() -> 
            challengeTodoService.cancelCompleteChallenge(participation.getChallenge().getId(), testMember, LocalDate.now())
        )
        .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("중도하차 후 투두 목록에서 제외됨")
    void getTodos_AfterLeavingChallenge() {
        var participation = allParticipations.get(0);
        
        var todosBeforeLeaving = challengeTodoService.getAllChallengeTodos(testMember);
        assertThat(todosBeforeLeaving).hasSize(3);
        
        participation.leaveChallenge();
        participationRepository.save(participation);
        
        var todosAfterLeaving = challengeTodoService.getAllChallengeTodos(testMember);
        assertThat(todosAfterLeaving).hasSize(2);
        
        var remainingPeriodTypes = todosAfterLeaving.stream()
                .map(ChallengeTodoDto::periodType)
                .toList();
        assertThat(remainingPeriodTypes).containsExactlyInAnyOrder(PeriodType.WEEKLY, PeriodType.MONTHLY);
    }

    @Test
    @DisplayName("중도하차한 챌린지에서 투두 완료 시도 시 예외 발생")
    void completeChallenge_AfterLeaving() {
        var participation = allParticipations.get(0);
        participation.leaveChallenge();
        participationRepository.save(participation);
        
        assertThatThrownBy(() -> 
            challengeTodoService.completeChallenge(participation.getChallenge().getId(), testMember, LocalDate.now())
        )
        .isInstanceOf(BusinessException.class)
        .hasMessage("해당 챌린지에 참여하지 않았습니다.");
    }
}
