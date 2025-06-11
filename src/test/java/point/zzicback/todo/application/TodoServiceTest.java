package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import point.zzicback.challenge.application.ChallengeParticipationService;
import point.zzicback.challenge.application.ChallengeTodoService;
import point.zzicback.challenge.application.ChallengeService;
import point.zzicback.challenge.application.mapper.ChallengeApplicationMapperImpl;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.infrastructure.ChallengeRepository;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.command.UpdateTodoCommand;
import point.zzicback.todo.application.dto.query.TodoListQuery;
import point.zzicback.todo.application.dto.query.TodoQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
// import point.zzicback.todo.application.mapper.TodoApplicationMapperImpl;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class,
    ChallengeParticipationService.class,
    ChallengeTodoService.class,
    ChallengeService.class,
    ChallengeApplicationMapperImpl.class
    // TodoApplicationMapperImpl.class  // MapStruct 구현체 제거
})
class TodoServiceTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipationService participationService;

    private Member testMember;
    private Todo testTodo;
    private Challenge testChallenge;
    private ChallengeParticipation testParticipation;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 생성
        CreateMemberCommand createMemberCommand = new CreateMemberCommand("test@test.com", "password", "tester");
        testMember = memberService.createMember(createMemberCommand);

        // 테스트용 Todo 생성
        testTodo = Todo.builder()
                .title("테스트 할일")
                .description("테스트 설명")
                .done(false)
                .member(testMember)
                .build();
        todoRepository.save(testTodo);

        // 테스트용 챌린지 생성 및 저장
        testChallenge = Challenge.builder()
                .title("테스트 챌린지")
                .description("챌린지 설명")
                .periodType(PeriodType.DAILY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(testChallenge);

        // 챌린지 참여 생성 및 저장 (participationService를 통해 저장)
        participationService.joinChallenge(testChallenge.getId(), testMember);
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 (일반 Todo)")
    void getTodoList_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        TodoListQuery query = TodoListQuery.of(testMember.getId(), false, pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1); // 일반 Todo 1개
        assertThat(result.getContent().get(0))
                .satisfies(todo -> {
                    assertThat(todo.title()).isEqualTo("테스트 할일");
                    assertThat(todo.description()).isEqualTo("테스트 설명");
                    assertThat(todo.done()).isFalse();
                });
    }

    @Test
    @DisplayName("Todo 생성 성공")
    void createTodo_Success() {
        // given
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "새로운 할일",
                "새로운 설명"
        );

        // when
        todoService.createTodo(command);

        // then
        List<Todo> todos = todoRepository.findByMemberIdAndDone(testMember.getId(), false, PageRequest.of(0, 10)).getContent();
        assertThat(todos)
                .filteredOn(todo -> todo.getTitle().equals("새로운 할일"))
                .hasSize(1)
                .first()
                .satisfies(todo -> {
                    assertThat(todo.getDescription()).isEqualTo("새로운 설명");
                    assertThat(todo.getDone()).isFalse();
                });
    }

    @Test
    @DisplayName("Todo 수정 성공")
    void updateTodo_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "수정된 할일",
                "수정된 설명",
                true
        );

        // when
        todoService.updateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("수정된 할일");
        assertThat(updatedTodo.getDescription()).isEqualTo("수정된 설명");
        assertThat(updatedTodo.getDone()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 Todo 수정 시 예외 발생")
    void updateTodo_NotFound() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                999L,
                "수정된 할일",
                "수정된 설명",
                true
        );

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Todo 삭제 성공")
    void deleteTodo_Success() {
        // when
        todoService.deleteTodo(TodoQuery.of(testMember.getId(), testTodo.getId()));

        // then
        assertThat(todoRepository.findById(testTodo.getId())).isEmpty();
    }
}