package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.*;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class
})
class TodoServiceTestNew {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        CreateMemberCommand memberCommand = new CreateMemberCommand("test@example.com", "password", "nickname", null);
        testMember = memberService.createMember(memberCommand);

        // 테스트용 Todo 생성
        testTodo = Todo.builder()
                .title("테스트 할일")
                .description("테스트 설명")
                .statusId(0)
                .dueDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .member(testMember)
                .build();
        todoRepository.save(testTodo);
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - status로 필터링")
    void getTodoListByStatus_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        TodoListQuery query = TodoListQuery.of(testMember.getId(), 0, pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst())
                .satisfies(todo -> {
                    assertThat(todo.title()).isEqualTo("테스트 할일");
                    assertThat(todo.description()).isEqualTo("테스트 설명");
                    assertThat(todo.statusId()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - 전체 조회")
    void getTodoListAll_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        TodoListQuery query = TodoListQuery.ofAll(testMember.getId(), pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst())
                .satisfies(todo -> {
                    assertThat(todo.title()).isEqualTo("테스트 할일");
                    assertThat(todo.description()).isEqualTo("테스트 설명");
                    assertThat(todo.statusId()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("Todo 생성 성공")
    void createTodo_Success() {
        // given
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "새로운 할일",
                "새로운 설명",
                null, null, null, null, null
        );

        // when
        todoService.createTodo(command);

        // then
        Page<Todo> todos = todoRepository.findByMemberId(
                testMember.getId(), null, null, null,
                PageRequest.of(0, 10));
        assertThat(todos.getContent())
                .filteredOn(todo -> todo.getTitle().equals("새로운 할일"))
                .hasSize(1)
                .first()
                .satisfies(todo -> {
                    assertThat(todo.getDescription()).isEqualTo("새로운 설명");
                    assertThat(todo.getStatusId()).isEqualTo(0);
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
                1,
                null, null, null, null, null
        );

        // when
        todoService.updateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("수정된 할일");
        assertThat(updatedTodo.getDescription()).isEqualTo("수정된 설명");
        assertThat(updatedTodo.getStatusId()).isEqualTo(1);
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
                1,
                null, null, null, null, null
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

    @Test
    @DisplayName("존재하지 않는 Todo 삭제 시 예외 발생")
    void deleteTodo_NotFound() {
        // given
        TodoQuery query = TodoQuery.of(testMember.getId(), 999L);

        // when & then
        assertThatThrownBy(() -> todoService.deleteTodo(query))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Todo 조회 성공")
    void getTodo_Success() {
        // given
        TodoQuery query = TodoQuery.of(testMember.getId(), testTodo.getId());

        // when
        TodoResult result = todoService.getTodo(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("테스트 할일");
        assertThat(result.description()).isEqualTo("테스트 설명");
        assertThat(result.statusId()).isEqualTo(0);
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - 날짜 범위 필터링")
    void getTodoListByDateRange() {
        // given
        Todo extraTodo = Todo.builder()
                .title("추가 할일")
                .description("다른 설명")
                .statusId(0)
                .dueDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .member(testMember)
                .build();
        todoRepository.save(extraTodo);

        Pageable pageable = PageRequest.of(0, 10);
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(3, ChronoUnit.DAYS);
        TodoListQuery query = TodoListQuery.of(
                testMember.getId(), null, null, null, null,
                null, start, end, pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent())
                .hasSize(0);

        // adjust range to include first todo
        query = TodoListQuery.of(
                testMember.getId(), null, null, null, null,
                null, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS), pageable);
        result = todoService.getTodoList(query);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
    }

    @Test
    @DisplayName("존재하지 않는 Todo 조회 시 예외 발생")
    void getTodo_NotFound() {
        // given
        TodoQuery query = TodoQuery.of(testMember.getId(), 999L);

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(query))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("여러 카테고리 ID로 태그 목록 조회")
    void getTagsByMultipleCategories() {
        Category category1 = categoryRepository.save(Category.builder()
                .name("업무")
                .member(testMember)
                .build());
        Category category2 = categoryRepository.save(Category.builder()
                .name("개인")
                .member(testMember)
                .build());

        Todo todo1 = Todo.builder()
                .title("첫 번째")
                .description("첫 번째 설명")
                .statusId(0)
                .category(category1)
                .dueDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .tags(Set.of("tagA"))
                .member(testMember)
                .build();
        Todo todo2 = Todo.builder()
                .title("두 번째")
                .description("두 번째 설명")
                .statusId(0)
                .category(category2)
                .dueDate(Instant.now().plus(2, ChronoUnit.DAYS))
                .tags(Set.of("tagB"))
                .member(testMember)
                .build();
        todoRepository.save(todo1);
        todoRepository.save(todo2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("tag"));
        Page<String> result = todoService.getTags(
                testMember.getId(),
                List.of(category1.getId(), category1.getId(), category2.getId()),
                pageable);

        assertThat(result.getContent()).containsExactlyInAnyOrder("tagA", "tagB");
    }
}
