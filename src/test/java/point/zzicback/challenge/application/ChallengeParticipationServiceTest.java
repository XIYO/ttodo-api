package point.zzicback.challenge.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.challenge.application.mapper.ChallengeApplicationMapperImpl;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.*;
import point.zzicback.member.domain.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    ChallengeParticipationService.class,
    ChallengeService.class,
    ChallengeApplicationMapperImpl.class
})
class ChallengeParticipationServiceTest {

    @Autowired
    private ChallengeParticipationService participationService;

    @Autowired
    private ChallengeParticipationRepository participationRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    private Member testMember;
    private Challenge testChallenge;
    private Challenge testChallenge2;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .build();
        memberRepository.save(testMember);

        testChallenge = Challenge.builder()
                .title("테스트 챌린지")
                .description("테스트용 챌린지 설명")
                .periodType(PeriodType.DAILY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(testChallenge);

        testChallenge2 = Challenge.builder()
                .title("테스트 챌린지 2")
                .description("두 번째 테스트용 챌린지 설명")
                .periodType(PeriodType.WEEKLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(testChallenge2);
    }

    @Test
    @DisplayName("챌린지 참여 성공")
    void joinChallenge_Success() {
        ChallengeParticipation participation = participationService.joinChallenge(testChallenge.getId(), testMember);

        assertThat(participation).isNotNull();
        assertThat(participation.getMember()).isEqualTo(testMember);
        assertThat(participation.getChallenge()).isEqualTo(testChallenge);
        assertThat(participation.isActive()).isTrue();
        assertThat(participation.hasLeftChallenge()).isFalse();
    }

    @Test
    @DisplayName("이미 참여중인 챌린지 참여 시 예외 발생")
    void joinChallenge_AlreadyJoined() {
        participationService.joinChallenge(testChallenge.getId(), testMember);

        assertThatThrownBy(() -> participationService.joinChallenge(testChallenge.getId(), testMember))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 참여중인 챌린지입니다.");
    }

    @Test
    @DisplayName("중도하차 성공")
    void leaveChallenge_Success() {
        ChallengeParticipation participation = participationService.joinChallenge(testChallenge.getId(), testMember);
        
        participationService.leaveChallenge(testChallenge.getId(), testMember);
        
        ChallengeParticipation updatedParticipation = participationRepository.findById(participation.getId()).orElseThrow();
        assertThat(updatedParticipation.hasLeftChallenge()).isTrue();
        assertThat(updatedParticipation.isActive()).isFalse();
        assertThat(updatedParticipation.getJoinOut()).isNotNull();
    }

    @Test
    @DisplayName("참여하지 않은 챌린지 중도하차 시 예외 발생")
    void leaveChallenge_NotParticipating() {
        assertThatThrownBy(() -> participationService.leaveChallenge(testChallenge.getId(), testMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("참여하지 않은 챌린지입니다.");
    }

    @Test
    @DisplayName("중도하차 후 재참여 가능")
    void rejoinAfterLeaving() {
        ChallengeParticipation firstParticipation = participationService.joinChallenge(testChallenge.getId(), testMember);
        participationService.leaveChallenge(testChallenge.getId(), testMember);
        
        ChallengeParticipation secondParticipation = participationService.joinChallenge(testChallenge.getId(), testMember);
        
        assertThat(secondParticipation.getId()).isNotEqualTo(firstParticipation.getId());
        assertThat(secondParticipation.isActive()).isTrue();
        
        var allParticipations = participationRepository.findAll();
        assertThat(allParticipations).hasSize(2);
        
        var activeParticipations = participationRepository.findByMemberAndJoinOutIsNull(testMember);
        assertThat(activeParticipations).hasSize(1);
        assertThat(activeParticipations.get(0).getId()).isEqualTo(secondParticipation.getId());
    }

    @Test
    @DisplayName("중도하차한 참여자의 투두 목록은 비어있음")
    void getChallengeTodos_AfterLeaving() {
        participationService.joinChallenge(testChallenge.getId(), testMember);
        participationService.leaveChallenge(testChallenge.getId(), testMember);
        
        var todos = participationService.getChallengeTodos(testMember);
        
        assertThat(todos).isEmpty();
    }

    @Test
    @DisplayName("챌린지 참여 후 완료된 투두가 없으면 빈 목록 반환")
    void joinChallenge_NoCompletedTodos() {
        participationService.joinChallenge(testChallenge.getId(), testMember);

        var todos = participationService.getChallengeTodos(testMember);

        assertThat(todos).isEmpty();
    }

    @Test
    @DisplayName("챌린지 중도하차 후 재참여 시 투두는 완료된 것만 반환")
    void rejoinChallenge_OnlyCompletedTodosReturned() {
        ChallengeParticipation participation = participationService.joinChallenge(testChallenge.getId(), testMember);
        participationService.leaveChallenge(testChallenge.getId(), testMember);

        participationService.joinChallenge(testChallenge.getId(), testMember);

        var todos = participationService.getChallengeTodos(testMember);

        assertThat(todos).isEmpty();
    }

    @Test
    @DisplayName("다른 챌린지 참여 시 기존 완료된 투두 유지됨")
    void joinDifferentChallenge_KeepsExistingCompletedTodos() {
        ChallengeParticipation participation1 = participationService.joinChallenge(testChallenge.getId(), testMember);
        
        ChallengeTodo todo1 = ChallengeTodo.builder()
                .challengeParticipation(participation1)
                .targetDate(LocalDate.now())
                .build();
        todo1.complete(LocalDate.now());
        challengeTodoRepository.save(todo1);

        var todosBefore = participationService.getChallengeTodos(testMember);

        participationService.joinChallenge(testChallenge2.getId(), testMember);

        var todosAfter = participationService.getChallengeTodos(testMember);

        assertThat(todosAfter).hasSize(todosBefore.size());
        assertThat(todosAfter).hasSize(1);
    }

    @Test
    @DisplayName("참여중인 챌린지의 완료된 투두 목록 조회")
    void getChallengeTodos_WithCompletedTodos() {
        // 첫 번째 챌린지 참여 및 완료
        ChallengeParticipation participation1 = participationService.joinChallenge(testChallenge.getId(), testMember);
        ChallengeTodo todo1 = ChallengeTodo.builder()
                .challengeParticipation(participation1)
                .targetDate(LocalDate.now())
                .build();
        todo1.complete(LocalDate.now());
        challengeTodoRepository.save(todo1);
        
        // 두 번째 챌린지 참여 및 완료
        ChallengeParticipation participation2 = participationService.joinChallenge(testChallenge2.getId(), testMember);
        ChallengeTodo todo2 = ChallengeTodo.builder()
                .challengeParticipation(participation2)
                .targetDate(LocalDate.now())
                .build();
        todo2.complete(LocalDate.now());
        challengeTodoRepository.save(todo2);
        
        var todos = participationService.getChallengeTodos(testMember);
        
        assertThat(todos).hasSize(2);
        assertThat(todos.stream().allMatch(ChallengeTodo::isCompleted)).isTrue();
        assertThat(todos.stream().map(todo -> todo.getChallengeParticipation().getChallenge().getTitle()))
                .containsExactlyInAnyOrder("테스트 챌린지", "테스트 챌린지 2");
    }
    
    @Test
    @DisplayName("참여중인 챌린지가 없을 때 빈 투두 목록 반환")
    void getChallengeTodos_NoParticipation() {
        var todos = participationService.getChallengeTodos(testMember);
        
        assertThat(todos).isEmpty();
    }
    
    @Test
    @DisplayName("완료된 투두가 없을 때 빈 투두 목록 반환")
    void getChallengeTodos_NoCompletedTodos() {
        participationService.joinChallenge(testChallenge.getId(), testMember);
        participationService.joinChallenge(testChallenge2.getId(), testMember);
        
        var todos = participationService.getChallengeTodos(testMember);
        
        assertThat(todos).isEmpty();
    }
    
    @Test
    @DisplayName("일부 챌린지만 완료된 경우")
    void getChallengeTodos_PartiallyCompleted() {
        // 첫 번째 챌린지만 완료
        ChallengeParticipation participation1 = participationService.joinChallenge(testChallenge.getId(), testMember);
        ChallengeTodo todo1 = ChallengeTodo.builder()
                .challengeParticipation(participation1)
                .targetDate(LocalDate.now())
                .build();
        todo1.complete(LocalDate.now());
        challengeTodoRepository.save(todo1);
        
        // 두 번째 챌린지는 참여만 하고 완료하지 않음
        participationService.joinChallenge(testChallenge2.getId(), testMember);
        
        var todos = participationService.getChallengeTodos(testMember);
        
        assertThat(todos).hasSize(1);
        assertThat(todos.get(0).getChallengeParticipation().getChallenge().getTitle()).isEqualTo("테스트 챌린지");
        assertThat(todos.get(0).isCompleted()).isTrue();
    }
}
