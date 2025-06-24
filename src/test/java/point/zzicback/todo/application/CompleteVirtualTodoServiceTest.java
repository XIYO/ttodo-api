package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.RepeatTypeConstants;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class,
    RepeatTodoService.class
})
class CompleteVirtualTodoServiceTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        CreateMemberCommand memberCommand = new CreateMemberCommand("test@example.com", "password", "nickname", null);
        testMember = memberService.createMember(memberCommand);
    }

    @Test
    @DisplayName("새로운 POST API로 가상 투두 완료 처리 성공")
    void completeVirtualTodo_Success() {
        CreateTodoCommand createCommand = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동",
                "조깅하기",
                1, null,
                LocalDate.of(2024, 1, 1),
                LocalTime.of(9, 0),
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 5),
                Set.of("운동")
        );
        todoService.createTodo(createCommand);

        CompleteVirtualTodoCommand completeCommand = new CompleteVirtualTodoCommand(
                testMember.getId(),
                1L, // 원본 투두 ID
                LocalDate.of(2024, 1, 2) // 완료할 날짜
        );

        TodoResult result = todoService.completeVirtualTodo(completeCommand);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("매일 운동");
        assertThat(result.statusId()).isEqualTo(1);
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(result.originalTodoId()).isEqualTo(1L);

        long totalTodos = todoRepository.countByMemberId(testMember.getId());
        assertThat(totalTodos).isEqualTo(2); // 원본 1개 + 완료된 투두 1개
    }
}
