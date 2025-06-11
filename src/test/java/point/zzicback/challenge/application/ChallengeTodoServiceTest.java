package point.zzicback.challenge.application;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.Import;
import point.zzicback.challenge.application.dto.result.ChallengeTodoDto;
import point.zzicback.challenge.application.mapper.ChallengeApplicationMapperImpl;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.*;
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
        assertThat(todos).hasSize(1); // 완료된 Todo 1개만 저장됨
        
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
        assertThat(todos).isEmpty(); // 완료 취소 시 Todo 삭제됨
    }

    @Test
    @DisplayName("미완료 투두만 있는 경우 - DAILY, WEEKLY, MONTHLY")
    void getTodos_OnlyIncomplete() {
        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).hasSize(3);
        assertThat(allTodos.stream().allMatch(dto -> !dto.done())).isTrue();
        assertThat(allTodos.stream().allMatch(dto -> !dto.isPersisted())).isTrue(); // 가상 Todo이므로 isPersisted = false

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
        completeChallengeTodo(allParticipations.get(0)); // DAILY만 완료

        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).hasSize(2);

        var completedTodos = allTodos.stream().filter(ChallengeTodoDto::done).toList();
        var incompleteTodos = allTodos.stream().filter(dto -> !dto.done()).toList();

        assertThat(completedTodos).hasSize(1);
        assertThat(incompleteTodos).hasSize(2);
        assertThat(completedTodos.get(0).periodType()).isEqualTo(PeriodType.DAILY);
        assertThat(completedTodos.get(0).isPersisted()).isTrue(); // 완료된 Todo는 DB에 저장됨

        var incompletePeriodTypes = incompleteTodos.stream().map(ChallengeTodoDto::periodType).toList();
        assertThat(incompletePeriodTypes).containsExactlyInAnyOrder(PeriodType.WEEKLY, PeriodType.MONTHLY);
        assertThat(incompleteTodos.stream().allMatch(dto -> !dto.isPersisted())).isTrue(); // 미완료 Todo는 가상 Todo
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
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("해당 챌린지에 참여하지 않았습니다.");
    }

    @Test
    @DisplayName("예외 처리 - Todo 찾을 수 없음")
    void cancelCompleteChallenge_TodoNotFound() {
        var participation = allParticipations.get(0);

        assertThatThrownBy(() -> 
            challengeTodoService.cancelCompleteChallenge(participation.getChallenge().getId(), testMember, LocalDate.now())
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("챌린지 Todo를 찾을 수 없습니다.");
    }
}
