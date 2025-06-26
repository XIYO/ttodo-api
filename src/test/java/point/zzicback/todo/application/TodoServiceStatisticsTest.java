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
import point.zzicback.todo.application.dto.result.TodoStatistics;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class,
    RepeatTodoService.class
})
class TodoServiceStatisticsTest {

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
    @DisplayName("Todo 통계 조회 성공")
    void getTodoStatistics_Success() {
        Todo testTodo = Todo.builder()
                .title("진행중 할일")
                .description("진행중 설명")
                .statusId(0)
                .member(testMember)
                .build();
        
        Todo completedTodo = Todo.builder()
                .title("완료된 할일")
                .description("완료된 설명")
                .statusId(1)
                .member(testMember)
                .build();
        
        Todo overdueTodo = Todo.builder()
                .title("지연된 할일")
                .description("지연된 설명")
                .statusId(0)
                .dueDate(LocalDate.now().minusDays(1))
                .member(testMember)
                .build();
        
        todoRepository.saveAll(List.of(testTodo, completedTodo, overdueTodo));

        TodoStatistics statistics = todoService.getTodoStatistics(testMember.getId());

        assertThat(statistics.total()).isEqualTo(3);
        assertThat(statistics.inProgress()).isEqualTo(1);
        assertThat(statistics.completed()).isEqualTo(1);
        assertThat(statistics.overdue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Todo 통계 조회 - 할일이 없는 경우")
    void getTodoStatistics_EmptyTodos_Success() {
        TodoStatistics statistics = todoService.getTodoStatistics(testMember.getId());

        assertThat(statistics.total()).isEqualTo(0);
        assertThat(statistics.inProgress()).isEqualTo(0);
        assertThat(statistics.completed()).isEqualTo(0);
        assertThat(statistics.overdue()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 Todo 통계 조회")
    void getTodoStatistics_NonExistentMember_Success() {
        UUID nonExistentMemberId = UUID.randomUUID();

        TodoStatistics statistics = todoService.getTodoStatistics(nonExistentMemberId);

        assertThat(statistics.total()).isEqualTo(0);
        assertThat(statistics.inProgress()).isEqualTo(0);
        assertThat(statistics.completed()).isEqualTo(0);
        assertThat(statistics.overdue()).isEqualTo(0);
    }
}
