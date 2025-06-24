package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.common.error.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.domain.*;
import point.zzicback.todo.infrastructure.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    RepeatTodoService.class,
    MemberService.class
})
class RepeatTodoServiceTest {

    @Autowired
    private RepeatTodoService repeatTodoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private RepeatTodoRepository repeatTodoRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        CreateMemberCommand memberCommand = new CreateMemberCommand(
                "test@example.com", "password", "testuser", null);
        testMember = memberService.createMember(memberCommand);

        testTodo = Todo.builder()
                .title("반복 할일")
                .description("반복 테스트")
                .statusId(0)
                .priorityId(1)
                .dueDate(LocalDate.of(2024, 1, 1))
                .dueTime(LocalTime.of(9, 0))
                .tags(Set.of("테스트"))
                .member(testMember)
                .build();
        todoRepository.save(testTodo);
    }

    @Test
    @DisplayName("반복 투두 생성 성공")
    void createRepeatTodo_Success() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 10);

        repeatTodoService.createRepeatTodo(
                testTodo,
                RepeatTypeConstants.DAILY,
                1,
                startDate,
                endDate,
                testMember
        );

        List<RepeatTodo> repeatTodos = repeatTodoRepository.findByMemberIdAndIsActiveTrue(testMember.getId());
        assertThat(repeatTodos).hasSize(1);

        RepeatTodo repeatTodo = repeatTodos.getFirst();
        assertThat(repeatTodo.getTodo().getId()).isEqualTo(testTodo.getId());
        assertThat(repeatTodo.getRepeatType()).isEqualTo(RepeatTypeConstants.DAILY);
        assertThat(repeatTodo.getRepeatInterval()).isEqualTo(1);
        assertThat(repeatTodo.getRepeatStartDate()).isEqualTo(startDate);
        assertThat(repeatTodo.getRepeatEndDate()).isEqualTo(endDate);
        assertThat(repeatTodo.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("활성 반복 투두 조회 성공")
    void getActiveRepeatTodos_Success() {
        repeatTodoService.createRepeatTodo(
                testTodo,
                RepeatTypeConstants.WEEKLY,
                2,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1),
                testMember
        );

        List<RepeatTodo> activeRepeatTodos = repeatTodoService.getActiveRepeatTodos(testMember.getId());
        assertThat(activeRepeatTodos).hasSize(1);
        assertThat(activeRepeatTodos.getFirst().getRepeatType()).isEqualTo(RepeatTypeConstants.WEEKLY);
        assertThat(activeRepeatTodos.getFirst().getRepeatInterval()).isEqualTo(2);
    }

    @Test
    @DisplayName("가상 날짜 생성 - 매일 반복")
    void generateVirtualDates_Daily() {
        RepeatTodo repeatTodo = RepeatTodo.builder()
                .todo(testTodo)
                .repeatType(RepeatTypeConstants.DAILY)
                .repeatInterval(1)
                .repeatStartDate(LocalDate.of(2024, 1, 1))
                .repeatEndDate(LocalDate.of(2024, 1, 5))
                .member(testMember)
                .build();

        List<LocalDate> virtualDates = repeatTodoService.generateVirtualDates(
                repeatTodo,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 5)
        );

        assertThat(virtualDates).hasSize(5);
        assertThat(virtualDates.get(0)).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(virtualDates.get(4)).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    @DisplayName("가상 날짜 생성 - 매주 반복")
    void generateVirtualDates_Weekly() {
        RepeatTodo repeatTodo = RepeatTodo.builder()
                .todo(testTodo)
                .repeatType(RepeatTypeConstants.WEEKLY)
                .repeatInterval(1)
                .repeatStartDate(LocalDate.of(2024, 1, 1))
                .repeatEndDate(LocalDate.of(2024, 1, 29))
                .member(testMember)
                .build();

        List<LocalDate> virtualDates = repeatTodoService.generateVirtualDates(
                repeatTodo,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 29)
        );

        assertThat(virtualDates).hasSize(5);
        assertThat(virtualDates.get(0)).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(virtualDates.get(1)).isEqualTo(LocalDate.of(2024, 1, 8));
        assertThat(virtualDates.get(4)).isEqualTo(LocalDate.of(2024, 1, 29));
    }

    @Test
    @DisplayName("가상 날짜 생성 - 매월 반복")
    void generateVirtualDates_Monthly() {
        RepeatTodo repeatTodo = RepeatTodo.builder()
                .todo(testTodo)
                .repeatType(RepeatTypeConstants.MONTHLY)
                .repeatInterval(1)
                .repeatStartDate(LocalDate.of(2024, 1, 1))
                .repeatEndDate(LocalDate.of(2024, 4, 1))
                .member(testMember)
                .build();

        List<LocalDate> virtualDates = repeatTodoService.generateVirtualDates(
                repeatTodo,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 4, 1)
        );

        assertThat(virtualDates).hasSize(4);
        assertThat(virtualDates.get(0)).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(virtualDates.get(1)).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(virtualDates.get(3)).isEqualTo(LocalDate.of(2024, 4, 1));
    }

    @Test
    @DisplayName("반복 투두 완료 처리 성공")
    void completeRepeatTodo_Success() {
        LocalDate completionDate = LocalDate.of(2024, 1, 2);

        repeatTodoService.completeRepeatTodo(
                testMember.getId(),
                testTodo.getId(),
                completionDate
        );

        List<Todo> completedTodos = todoRepository.findAllByMemberId(testMember.getId());
        Todo completedTodo = completedTodos.stream()
                .filter(todo -> todo.getOriginalTodoId() != null)
                .findFirst()
                .orElseThrow();

        assertThat(completedTodo.getStatusId()).isEqualTo(1);
        assertThat(completedTodo.getDueDate()).isEqualTo(completionDate);
        assertThat(completedTodo.getOriginalTodoId()).isEqualTo(testTodo.getId());
        assertThat(completedTodo.getTitle()).isEqualTo(testTodo.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 원본 투두로 완료 시도시 예외 발생")
    void completeRepeatTodo_OriginalTodoNotFound() {
        assertThatThrownBy(() -> repeatTodoService.completeRepeatTodo(
                testMember.getId(),
                999L,
                LocalDate.of(2024, 1, 2)
        )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("이미 완료된 투두 중복 완료 방지")
    void completeRepeatTodo_DuplicatePrevention() {
        LocalDate completionDate = LocalDate.of(2024, 1, 2);

        repeatTodoService.completeRepeatTodo(
                testMember.getId(),
                testTodo.getId(),
                completionDate
        );

        assertThatThrownBy(() -> repeatTodoService.completeRepeatTodo(
                testMember.getId(),
                testTodo.getId(),
                completionDate
        )).isInstanceOf(BusinessException.class)
          .hasMessageContaining("이미 완료된 투두입니다");
    }

    @Test
    @DisplayName("다른 사용자의 투두 완료 시도시 예외 발생")
    void completeRepeatTodo_UnauthorizedAccess() {
        CreateMemberCommand anotherMemberCommand = new CreateMemberCommand(
                "another@example.com", "password", "anotheruser", null);
        Member anotherMember = memberService.createMember(anotherMemberCommand);

        assertThatThrownBy(() -> repeatTodoService.completeRepeatTodo(
                anotherMember.getId(),
                testTodo.getId(),
                LocalDate.of(2024, 1, 2)
        )).isInstanceOf(EntityNotFoundException.class);
    }
}
