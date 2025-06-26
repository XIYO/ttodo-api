package point.zzicback.todo.application;

import java.time.LocalDate;
import java.time.LocalTime;
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
class VirtualTodoIdTest {

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
    @DisplayName("가상 투두 ID 형식 확인 테스트 - '원본ID:반복순서' 형식")
    void virtualTodoIdFormatTest() {
        // given - 반복 투두 생성
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동하기",
                "30분 운동",
                1, null, 
                LocalDate.of(2024, 1, 1),
                LocalTime.of(9, 0),
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 5),
                null, // daysOfWeek
                Set.of("건강")
        );
        
        todoService.createTodo(command);
        
        // 원본 투두 ID 확인
        Long originalTodoId = todoRepository.findAllByMemberId(testMember.getId()).get(0).getId();
        
        // when - 가상 투두 목록 조회
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 5),
                PageRequest.of(0, 10)
        );
        Page<TodoResult> result = todoService.getTodoList(query);
        
        // then - 가상 투두들의 ID 형식 확인
        result.getContent().stream()
                .filter(todo -> todo.id() != null && todo.id().contains(":"))
                .forEach(virtualTodo -> {
                    String virtualId = virtualTodo.id();
                    System.out.println("가상 투두 ID: " + virtualId);
                    
                    // ID가 "원본ID:반복순서" 형식인지 확인
                    assertThat(virtualId).contains(":");
                    
                    String[] parts = virtualId.split(":");
                    assertThat(parts).hasSize(2);
                    
                    // 첫 번째 부분이 원본 투두 ID인지 확인
                    assertThat(parts[0]).isEqualTo(String.valueOf(originalTodoId));
                    
                    // 두 번째 부분이 숫자인지 확인
                    assertThat(parts[1]).matches("\\d+");
                    
                    // originalTodoId가 올바르게 설정되어 있는지 확인
                    assertThat(virtualTodo.originalTodoId()).isEqualTo(originalTodoId);
                });
        
        // 가상 투두가 정확히 4개 생성되었는지 확인 (1/2, 1/3, 1/4, 1/5)
        long virtualTodoCount = result.getContent().stream()
                .filter(todo -> todo.id() != null && todo.id().contains(":"))
                .count();
        assertThat(virtualTodoCount).isEqualTo(4);
    }
}
