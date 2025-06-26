package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.RepeatTypeConstants;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class,
    RepeatTodoService.class
})
class VirtualTodoDateFilterTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        CreateMemberCommand memberCommand = new CreateMemberCommand("test@example.com", "password", "nickname", null);
        testMember = memberService.createMember(memberCommand);
    }

    @Test
    @DisplayName("기준 날짜(date)를 사용하여 과거 가상 투두를 필터링")
    void filterVirtualTodosWithBaseDate() {
        // given - 매일 반복하는 투두 생성
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동하기",
                "헬스장에서 운동",
                1,
                null,
                LocalDate.of(2024, 1, 1),
                null,
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 10),
                null,
                Set.of("운동")
        );
        todoService.createTodo(command);

        // when - 1월 1일부터 1월 5일까지 조회하되, 기준 날짜를 1월 3일로 설정
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                LocalDate.of(2024, 1, 1), // 시작일
                LocalDate.of(2024, 1, 5), // 종료일
                LocalDate.of(2024, 1, 3), // 기준 날짜 (이 날짜 이후만 표시)
                PageRequest.of(0, 10)
        );
        Page<TodoResult> todos = todoService.getTodoList(query);

        // then - 1월 3일, 4일, 5일만 표시되어야 함 (1월 1일, 2일은 기준 날짜보다 과거이므로 제외)
        assertThat(todos.getContent()).hasSize(4); // 원본(1월 1일) + 가상(1월 3일, 4일, 5일)
        
        // 날짜별로 확인
        var dueDates = todos.getContent().stream()
                .map(TodoResult::dueDate)
                .sorted()
                .toList();
                
        assertThat(dueDates).containsExactly(
                LocalDate.of(2024, 1, 1), // 원본 투두
                LocalDate.of(2024, 1, 3), // 기준 날짜부터 시작
                LocalDate.of(2024, 1, 4),
                LocalDate.of(2024, 1, 5)
        );
    }

    @Test
    @DisplayName("기준 날짜가 없으면 시작 날짜를 기준으로 사용")
    void useStartDateWhenBaseDateIsNull() {
        // given - 매일 반복하는 투두 생성
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 독서하기",
                "책 읽기",
                1,
                null,
                LocalDate.of(2024, 1, 1),
                null,
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 5),
                null,
                Set.of("독서")
        );
        todoService.createTodo(command);

        // when - 기준 날짜 없이 조회 (시작 날짜부터 표시)
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                LocalDate.of(2024, 1, 2), // 시작일
                LocalDate.of(2024, 1, 5), // 종료일
                null, // 기준 날짜 없음
                PageRequest.of(0, 10)
        );
        Page<TodoResult> todos = todoService.getTodoList(query);

        // then - 시작 날짜인 1월 2일부터 표시
        assertThat(todos.getContent()).hasSize(4); // 가상(1월 2일, 3일, 4일, 5일)
        
        var dueDates = todos.getContent().stream()
                .map(TodoResult::dueDate)
                .sorted()
                .toList();
                
        assertThat(dueDates).containsExactly(
                LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3),
                LocalDate.of(2024, 1, 4),
                LocalDate.of(2024, 1, 5)
        );
    }
}
