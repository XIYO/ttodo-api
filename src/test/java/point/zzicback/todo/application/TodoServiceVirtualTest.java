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
                null, // daysOfWeek
                Set.of("건강")
        );
        
        todoService.createTodo(command);
        
        assertThat(todoRepository.findAllByMemberId(testMember.getId())).hasSize(1);
        assertThat(repeatTodoRepository.findByMemberIdAndIsActiveTrue(testMember.getId())).hasSize(1);
    }

    @Test
    @DisplayName("원본 투두와 가상 투두 조회 테스트")
    void getOriginalAndVirtualTodos() {
        // 고정된 날짜 사용으로 일관된 결과 보장
        LocalDate baseDate = LocalDate.of(2025, 1, 1);
        
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 독서하기",
                "책 10페이지 읽기",
                1, null, // statusId=1 (완료)
                baseDate,
                LocalTime.of(20, 0),
                RepeatTypeConstants.DAILY,
                1,
                baseDate,
                baseDate.plusDays(3),
                null, // daysOfWeek
                Set.of("학습")
        );
        
        todoService.createTodo(command);
        
        // 생성된 Todo 직접 확인
        var savedTodos = todoRepository.findAllByMemberId(testMember.getId());
        System.out.println("저장된 Todo 개수: " + savedTodos.size());
        if (!savedTodos.isEmpty()) {
            var todo = savedTodos.get(0);
            System.out.println("저장된 Todo - ID: " + todo.getId() + 
                ", StatusId: " + todo.getStatusId() + 
                ", DueDate: " + todo.getDueDate());
        }
        
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(0), // 진행중인 것만 조회 (가상 Todo가 statusId=0으로 생성됨)
                null, null, null, null, null,
                baseDate,
                baseDate.plusDays(2),
                PageRequest.of(0, 10)
        );
        
        Page<TodoResult> result = todoService.getTodoList(query);
        
        // 실제 저장된 Todo가 있는지 확인
        System.out.println("=== 실제 저장된 모든 Todo 목록 ===");
        todoRepository.findAllByMemberId(testMember.getId()).forEach(todo -> {
            System.out.println("실제 DB Todo - ID: " + todo.getId() + ", StatusId: " + todo.getStatusId() + 
                ", DueDate: " + todo.getDueDate() + ", Title: " + todo.getTitle());
        });
        
        // 디버깅을 위한 로그 추가
        System.out.println("=== 조회된 Todo 목록 ===");
        result.getContent().forEach(todo -> {
            System.out.println("ID: " + todo.id() + ", Title: " + todo.title() + ", DueDate: " + todo.dueDate() + ", OriginalTodoId: " + todo.originalTodoId());
        });
        System.out.println("총 개수: " + result.getContent().size());
        
        assertThat(result.getContent()).hasSize(2); // 원본 Todo는 완료상태(statusId=1)이므로 조회되지 않고, 가상 Todo 2개만 조회됨
        
        long originalCount = result.getContent().stream()
                .filter(todo -> todo.id() != null && !todo.id().contains(":"))
                .count();
        
        long virtualCount = result.getContent().stream()
                .filter(todo -> todo.id() != null && todo.id().contains(":") && todo.originalTodoId() != null)
                .count();
        
        assertThat(originalCount).isEqualTo(0); // 원본 Todo는 완료상태이므로 statusIds=[0] 조건에 맞지 않음
        assertThat(virtualCount).isEqualTo(2); // 가상 Todo는 statusId=0으로 생성되므로 2개 조회됨
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
                null, null, null, null, null,
                Set.of("학습")
        );
        
        CreateTodoCommand mathCommand = new CreateTodoCommand(
                testMember.getId(),
                "수학 공부하기",
                "미적분 문제 풀기",
                1, null, 
                today,
                LocalTime.of(10, 0),
                null, null, null, null, null,
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
                null, null, null, null, null,
                Set.of("업무")
        );
        
        todoService.createTodo(command);
        
        assertThat(todoRepository.findAllByMemberId(testMember.getId())).hasSize(1);
        assertThat(repeatTodoRepository.findByMemberIdAndIsActiveTrue(testMember.getId())).hasSize(0);
    }
}
