package point.zzicback.todo.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

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
import point.zzicback.todo.infrastructure.persistence.RepeatTodoRepository;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class,
    RepeatTodoService.class
})
class TodoServiceVirtualTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private RepeatTodoRepository repeatTodoRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        CreateMemberCommand memberCommand = new CreateMemberCommand("test@example.com", "password", "nickname", null);
        testMember = memberService.createMember(memberCommand);
        todoRepository.deleteAllByMemberId(testMember.getId());
        repeatTodoRepository.deleteAll();
    }

    @Test
    @DisplayName("반복 투두 생성 테스트")
    void createRepeatTodo() {
        LocalDate today = LocalDate.now();
        
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동하기",
                "30분 운동",
                1, null, 
                today,
                LocalTime.of(9, 0),
                RepeatTypeConstants.DAILY,
                1,
                today,
                today.plusDays(5),
                Set.of("건강")
        );
        
        todoService.createTodo(command);
        
        assertThat(todoRepository.findAllByMemberId(testMember.getId())).hasSize(1);
        assertThat(repeatTodoRepository.findByMemberIdAndIsActiveTrue(testMember.getId())).hasSize(1);
    }

    @Test
    @DisplayName("원본 투두와 가상 투두 조회 테스트")
    void getOriginalAndVirtualTodos() {
        LocalDate today = LocalDate.now();
        
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 독서하기",
                "책 10페이지 읽기",
                1, null, 
                today,
                LocalTime.of(20, 0),
                RepeatTypeConstants.DAILY,
                1,
                today,
                today.plusDays(3),
                Set.of("학습")
        );
        
        todoService.createTodo(command);
        
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(0),
                null, null, null, null, null,
                today,
                today.plusDays(2),
                PageRequest.of(0, 10)
        );
        
        Page<TodoResult> result = todoService.getTodoList(query);
        
        assertThat(result.getContent()).hasSize(3);
        
        long originalCount = result.getContent().stream()
                .filter(todo -> todo.id() != null)
                .count();
        
        long virtualCount = result.getContent().stream()
                .filter(todo -> todo.id() == null && todo.originalTodoId() != null)
                .count();
        
        assertThat(originalCount).isEqualTo(1);
        assertThat(virtualCount).isEqualTo(2);
    }

    @Test
    @DisplayName("키워드 검색 테스트")
    void searchTodosByKeyword() {
        LocalDate today = LocalDate.now();
        
        CreateTodoCommand englishCommand = new CreateTodoCommand(
                testMember.getId(),
                "영어 공부하기",
                "토익 문제 풀기",
                1, null, 
                today,
                LocalTime.of(9, 0),
                null, null, null, null,
                Set.of("학습")
        );
        
        CreateTodoCommand mathCommand = new CreateTodoCommand(
                testMember.getId(),
                "수학 공부하기",
                "미적분 문제 풀기",
                1, null, 
                today,
                LocalTime.of(10, 0),
                null, null, null, null,
                Set.of("학습")
        );
        
        todoService.createTodo(englishCommand);
        todoService.createTodo(mathCommand);
        
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, 
                "영어",
                null,
                today,
                today,
                PageRequest.of(0, 10)
        );
        
        Page<TodoResult> result = todoService.getTodoList(query);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).contains("영어");
    }

    @Test
    @DisplayName("일반 투두 생성 테스트")
    void createNormalTodo() {
        LocalDate today = LocalDate.now();
        
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "일회성 작업",
                "문서 정리하기",
                1, null, 
                today,
                LocalTime.of(14, 0),
                null, null, null, null,
                Set.of("업무")
        );
        
        todoService.createTodo(command);
        
        assertThat(todoRepository.findAllByMemberId(testMember.getId())).hasSize(1);
        assertThat(repeatTodoRepository.findByMemberIdAndIsActiveTrue(testMember.getId())).hasSize(0);
    }
}
