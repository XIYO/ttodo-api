package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.UpdateTodoCommand;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class
})
class TodoServicePartialUpdateTest {

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
        CreateMemberCommand memberCommand = new CreateMemberCommand("test@example.com", "password", "nickname", null);
        testMember = memberService.createMember(memberCommand);

        testTodo = Todo.builder()
                .title("테스트 할일")
                .description("테스트 설명")
                .statusId(0)
                .dueDate(LocalDate.now().plusDays(1))
                .tags(Set.of("학습"))
                .member(testMember)
                .build();
        todoRepository.save(testTodo);
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 제목만 수정")
    void partialUpdateTodo_TitleOnly_Success() {
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "부분 수정된 제목",
                null, null, null, null, null, null, null, null, null, null
        );

        todoService.partialUpdateTodo(command);

        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("부분 수정된 제목");
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명");
        assertThat(updatedTodo.getStatusId()).isEqualTo(0);
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 설명만 수정")
    void partialUpdateTodo_DescriptionOnly_Success() {
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null,
                "부분 수정된 설명",
                null, null, null, null, null, null, null, null, null
        );

        todoService.partialUpdateTodo(command);

        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일");
        assertThat(updatedTodo.getDescription()).isEqualTo("부분 수정된 설명");
        assertThat(updatedTodo.getStatusId()).isEqualTo(0);
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 상태만 수정")
    void partialUpdateTodo_StatusOnly_Success() {
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null, null,
                1,
                null, null, null, null, null, null, null, null
        );

        todoService.partialUpdateTodo(command);

        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일");
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명");
        assertThat(updatedTodo.getStatusId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 우선순위와 태그 수정")
    void partialUpdateTodo_PriorityAndTags_Success() {
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null, null, null,
                2,
                null, null, null, null, null, null,
                Set.of("업무", "중요")
        );

        todoService.partialUpdateTodo(command);

        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일");
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명");
        assertThat(updatedTodo.getStatusId()).isEqualTo(0);
        assertThat(updatedTodo.getPriorityId()).isEqualTo(2);
        assertThat(updatedTodo.getTags()).containsExactlyInAnyOrder("업무", "중요");
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 카테고리 수정")
    void partialUpdateTodo_CategoryOnly_Success() {
        Category newCategory = Category.builder()
                .name("새 카테고리")
                .member(testMember)
                .build();
        categoryRepository.save(newCategory);

        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null, null, null, null,
                newCategory.getId(),
                null, null, null, null, null, null
        );

        todoService.partialUpdateTodo(command);

        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일");
        assertThat(updatedTodo.getCategory().getName()).isEqualTo("새 카테고리");
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 빈 문자열 제목은 무시")
    void partialUpdateTodo_EmptyTitleIgnored_Success() {
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "   ",
                "수정된 설명",
                null, null, null, null, null, null, null, null, null
        );

        todoService.partialUpdateTodo(command);

        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일");
        assertThat(updatedTodo.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 빈 문자열 설명은 무시")
    void partialUpdateTodo_EmptyDescriptionIgnored_Success() {
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "수정된 제목",
                "",
                null, null, null, null, null, null, null, null, null
        );

        todoService.partialUpdateTodo(command);

        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명");
    }

    @Test
    @DisplayName("존재하지 않는 Todo 부분 수정 시 예외 발생")
    void partialUpdateTodo_NotFound() {
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                999L,
                "수정된 제목",
                null, null, null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> todoService.partialUpdateTodo(command))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
