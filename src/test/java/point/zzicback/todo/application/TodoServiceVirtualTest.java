package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.command.CompleteVirtualTodoCommand;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.RepeatTypeConstants;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;
import point.zzicback.todo.infrastructure.persistence.RepeatTodoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

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
        
        // 해당 멤버의 모든 투두와 반복 투두 데이터 정리
        List<Todo> memberTodos = todoRepository.findAllByMemberId(testMember.getId());
        for (Todo todo : memberTodos) {
            repeatTodoRepository.deleteByTodoId(todo.getId());
        }
        todoRepository.deleteAllByMemberId(testMember.getId());
    }

    @Test
    @DisplayName("statusId=0으로 검색시 원본 투두와 가상 투두 모두 표시")
    void getTodoList_WithStatusId0_ShowsBothOriginalAndVirtualTodos() {
        todoRepository.deleteAll();
        repeatTodoRepository.deleteAll();
        
        LocalDate today = LocalDate.now();
        
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 물마시기",
                "물 2L 마시기",
                1, null, 
                today, // 오늘 날짜로 원본 투두 생성
                LocalTime.of(23, 59), // 늦은 시간으로 설정하여 overdue 방지
                RepeatTypeConstants.DAILY,
                1,
                today, // 반복 시작일
                LocalDate.now().plusDays(5),
                Set.of("건강")
        );
        todoService.createTodo(command);

        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(0), // statusId=0만 검색
                null, null, null, null, null,
                today, // 오늘부터
                LocalDate.now().plusDays(1), // 내일까지 (오늘 원본 + 내일 가상 투두 포함)
                PageRequest.of(0, 10)
        );
        
        Page<TodoResult> todos = todoService.getTodoList(query);
        
        // 디버깅: 실제 DB에 있는 모든 투두 확인
        List<Todo> allDbTodos = todoRepository.findAllByMemberId(testMember.getId());
        System.out.println("DB에 있는 모든 투두 수: " + allDbTodos.size());
        for (Todo todo : allDbTodos) {
            System.out.println("DB 투두: ID=" + todo.getId() + ", title=" + todo.getTitle() + 
                             ", dueDate=" + todo.getDueDate() + ", statusId=" + todo.getStatusId());
        }
        
        // 특정 조건으로 직접 쿼리해보기
        System.out.println("특정 조건으로 직접 쿼리:");
        Page<Todo> directQuery = todoRepository.findByFilters(
            testMember.getId(), 
            List.of(0), // statusIds
            null, null, null, null, null,
            yesterday, // startDate
            LocalDate.now().plusDays(1), // endDate  
            PageRequest.of(0, 10)
        );
        System.out.println("직접 쿼리 결과 수: " + directQuery.getContent().size());
        for (Todo todo : directQuery.getContent()) {
            System.out.println("직접 쿼리 투두: ID=" + todo.getId() + ", title=" + todo.getTitle() + 
                             ", dueDate=" + todo.getDueDate() + ", statusId=" + todo.getStatusId());
        }
        
        // 디버깅: 검색 조건 확인
        System.out.println("검색 조건:");
        System.out.println("  statusIds: " + query.statusIds());
        System.out.println("  startDate: " + query.startDate());
        System.out.println("  endDate: " + query.endDate());
        System.out.println("  hideStatusIds: " + query.hideStatusIds());
        System.out.println("검색 결과 투두 수: " + todos.getContent().size());
        for (TodoResult todo : todos.getContent()) {
            System.out.println("검색 결과: ID=" + todo.id() + ", title=" + todo.title() + 
                             ", dueDate=" + todo.dueDate() + ", statusId=" + todo.statusId() + ", originalTodoId=" + todo.originalTodoId());
        }

        assertThat(todos.getContent()).hasSize(2);
        
        // 원본 투두 확인 (오늘 날짜)
        TodoResult originalTodo = todos.getContent().stream()
                .filter(todo -> todo.id() != null)
                .findFirst()
                .orElseThrow();
        assertThat(originalTodo.dueDate()).isEqualTo(LocalDate.now());
        assertThat(originalTodo.statusId()).isEqualTo(0);
        
        // 가상 투두 확인 (내일 날짜)
        TodoResult virtualTodo = todos.getContent().stream()
                .filter(todo -> todo.id() == null && todo.originalTodoId() != null)
                .findFirst()
                .orElseThrow();
        assertThat(virtualTodo.dueDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(virtualTodo.statusId()).isEqualTo(0);
    }

    @Test
    @DisplayName("DB에 투두가 저장되는지 확인")
    void testTodoSaving() {
        todoRepository.deleteAll();
        repeatTodoRepository.deleteAll();
        
        LocalDate today = LocalDate.now();
        
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 물마시기",
                "물 2L 마시기",
                1, null, 
                today,
                LocalTime.of(23, 59),
                RepeatTypeConstants.DAILY,
                1,
                today,
                LocalDate.now().plusDays(5),
                Set.of("건강")
        );
        todoService.createTodo(command);
        
        // DB에서 직접 조회
        List<Todo> allTodos = todoRepository.findAllByMemberId(testMember.getId());
        System.out.println("DB에 저장된 투두 수: " + allTodos.size());
        for (Todo todo : allTodos) {
            System.out.println("저장된 투두: ID=" + todo.getId() + ", title=" + todo.getTitle() + 
                             ", dueDate=" + todo.getDueDate() + ", statusId=" + todo.getStatusId());
        }
        
        assertThat(allTodos).hasSize(1);
        assertThat(allTodos.get(0).getStatusId()).isEqualTo(0);
        assertThat(allTodos.get(0).getDueDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("오늘부터 3일간 반복투두 가상투두 생성 테스트")
    void testVirtualTodoGeneration() {
        todoRepository.deleteAll();
        repeatTodoRepository.deleteAll();
        
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);
        
        // 오늘부터 3일간 매일 반복하는 투두 생성
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 물마시기",
                "물 2L 마시기",
                1, null, 
                today, // 오늘 날짜로 원본 투두 생성
                LocalTime.of(23, 59), // 늦은 시간으로 설정하여 overdue 방지
                RepeatTypeConstants.DAILY,
                1,
                today, // 반복 시작일 = 오늘
                today.plusDays(5), // 반복 종료일
                Set.of("건강")
        );
        todoService.createTodo(command);

        // 오늘부터 3일간 검색 (오늘 원본 + 내일, 모레 가상투두)
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(0), // statusId=0만 검색
                null, null, null, null, null,
                today, // 오늘부터
                dayAfterTomorrow, // 모레까지 (3일간)
                PageRequest.of(0, 10)
        );
        
        Page<TodoResult> todos = todoService.getTodoList(query);
        
        // 디버깅: 실제 DB에 있는 모든 투두 확인
        List<Todo> allDbTodos = todoRepository.findAllByMemberId(testMember.getId());
        System.out.println("DB에 있는 모든 투두 수: " + allDbTodos.size());
        for (Todo todo : allDbTodos) {
            System.out.println("DB 투두: ID=" + todo.getId() + ", title=" + todo.getTitle() + 
                             ", dueDate=" + todo.getDueDate() + ", statusId=" + todo.getStatusId());
        }
        
        // 디버깅: 검색 조건 확인
        System.out.println("검색 조건:");
        System.out.println("  statusIds: " + query.statusIds());
        System.out.println("  startDate: " + query.startDate());
        System.out.println("  endDate: " + query.endDate());
        System.out.println("검색 결과 투두 수: " + todos.getContent().size());
        for (TodoResult todo : todos.getContent()) {
            System.out.println("검색 결과: ID=" + todo.id() + ", title=" + todo.title() + 
                             ", dueDate=" + todo.dueDate() + ", statusId=" + todo.statusId() + 
                             ", originalTodoId=" + todo.originalTodoId());
        }

        // 예상: 3개 투두 (오늘 원본 + 내일 가상투두 + 모레 가상투두)
        assertThat(todos.getContent()).hasSize(3);
        
        // 오늘 투두 확인 (원본 투두)
        TodoResult originalTodo = todos.getContent().stream()
                .filter(todo -> todo.dueDate().equals(today))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("오늘 날짜의 원본 투두를 찾을 수 없습니다"));
        assertThat(originalTodo.id()).isNotNull(); // 원본 투두는 ID가 있어야 함
        assertThat(originalTodo.statusId()).isEqualTo(0);
        
        // 내일 투두 확인 (가상 투두)
        TodoResult tomorrowTodo = todos.getContent().stream()
                .filter(todo -> todo.dueDate().equals(tomorrow))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("내일 날짜의 가상 투두를 찾을 수 없습니다"));
        assertThat(tomorrowTodo.id()).isNull(); // 가상 투두는 ID가 null
        assertThat(tomorrowTodo.originalTodoId()).isNotNull(); // 원본 투두 ID가 있어야 함
        assertThat(tomorrowTodo.statusId()).isEqualTo(0);
        
        // 모레 투두 확인 (가상 투두)
        TodoResult dayAfterTomorrowTodo = todos.getContent().stream()
                .filter(todo -> todo.dueDate().equals(dayAfterTomorrow))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("모레 날짜의 가상 투두를 찾을 수 없습니다"));
        assertThat(dayAfterTomorrowTodo.id()).isNull(); // 가상 투두는 ID가 null
        assertThat(dayAfterTomorrowTodo.originalTodoId()).isNotNull(); // 원본 투두 ID가 있어야 함
        assertThat(dayAfterTomorrowTodo.statusId()).isEqualTo(0);
    }

    @Test
    @DisplayName("키워드 검색 테스트")
    void testKeywordSearch() {
        todoRepository.deleteAll();
        repeatTodoRepository.deleteAll();
        
        // 영어 관련 투두 생성
        CreateTodoCommand englishCommand = new CreateTodoCommand(
                testMember.getId(),
                "영어 공부하기",
                "토익 문제집 2장 풀기",
                1, null, 
                LocalDate.now(),
                LocalTime.of(9, 0),
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                Set.of("학습", "영어")
        );
        todoService.createTodo(englishCommand);
        
        // 수학 관련 투두 생성
        CreateTodoCommand mathCommand = new CreateTodoCommand(
                testMember.getId(),
                "수학 공부하기",
                "미적분 개념 정리",
                1, null, 
                LocalDate.now(),
                LocalTime.of(10, 0),
                null, null, null, null,
                Set.of("학습", "수학")
        );
        todoService.createTodo(mathCommand);
        
        // "영어" 키워드로 검색
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, 
                "영어", // 키워드
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                PageRequest.of(0, 10)
        );
        
        Page<TodoResult> todos = todoService.getTodoList(query);
        
        System.out.println("키워드 '영어' 검색 결과:");
        for (TodoResult todo : todos.getContent()) {
            System.out.println("ID=" + todo.id() + ", title=" + todo.title() + 
                             ", originalTodoId=" + todo.originalTodoId());
        }
        
        // "영어"가 포함된 투두들만 나와야 함
        assertThat(todos.getContent()).allMatch(todo -> 
            todo.title().contains("영어") || 
            todo.description().contains("영어") ||
            todo.tags().contains("영어"));
    }
}
