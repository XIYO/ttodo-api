package point.zzicback.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.persistance.TodoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TodoServiceImpl todoService;

    @Test
    @DisplayName("할 일 목록 조회 테스트")
    void getTodoListTest() {
        // Given
        Todo todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("첫 번째 할 일");
        todo1.setDescription("첫 번째 설명");
        todo1.setDone(false);

        Todo todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("두 번째 할 일");
        todo2.setDescription("두 번째 설명");
        todo2.setDone(false);

        when(todoRepository.findByDone(false)).thenReturn(Arrays.asList(todo1, todo2));

        // When
        List<Todo> result = todoService.getTodoList(false);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("첫 번째 할 일");
        assertThat(result.get(1).getTitle()).isEqualTo("두 번째 할 일");
        verify(todoRepository, times(1)).findByDone(false);
    }

    @Test
    @DisplayName("회원별 할 일 목록 조회 테스트")
    void getTodoListByMemberTest() {
        // Given
        UUID memberId = UUID.randomUUID();

        Todo todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("첫 번째 할 일");
        todo1.setDescription("첫 번째 설명");
        todo1.setDone(false);

        Todo todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("두 번째 할 일");
        todo2.setDescription("두 번째 설명");
        todo2.setDone(false);

        when(todoRepository.findByMemberIdAndDone(memberId, false)).thenReturn(Arrays.asList(todo1, todo2));

        // When
        List<Todo> result = todoService.getTodoListByMember(memberId, false);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("첫 번째 할 일");
        assertThat(result.get(1).getTitle()).isEqualTo("두 번째 할 일");
        verify(todoRepository, times(1)).findByMemberIdAndDone(memberId, false);
    }

    @Test
    @DisplayName("할 일 생성 테스트")
    void createTodoTest() {
        // Given
        UUID memberId = UUID.randomUUID();

        Member member = new Member();
        member.setId(memberId);
        member.setEmail("test@example.com");
        member.setNickname("테스터");

        Todo todo = new Todo();
        todo.setTitle("새로운 할 일");
        todo.setDescription("설명");
        todo.setDone(false);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        // When
        todoService.createTodo(memberId, todo);

        // Then
        assertThat(todo.getMember()).isEqualTo(member);
        verify(memberRepository, times(1)).findById(memberId);
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    @DisplayName("할 일 수정 테스트")
    void updateTodoTest() {
        // Given
        UUID memberId = null;
        Long todoId = 1L;

        Todo existingTodo = new Todo();
        existingTodo.setId(todoId);
        existingTodo.setTitle("기존 할 일");
        existingTodo.setDescription("기존 설명");
        existingTodo.setDone(false);

        Todo updatedTodo = new Todo();
        updatedTodo.setId(todoId);
        updatedTodo.setTitle("수정된 할 일");
        updatedTodo.setDescription("수정된 설명");
        updatedTodo.setDone(true);

        when(todoRepository.findByIdAndMember_Id(todoId, memberId)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(existingTodo);

        // When
        todoService.updateTodo(memberId, updatedTodo);

        // Then
        verify(todoRepository, times(1)).findByIdAndMember_Id(todoId, memberId);
        verify(todoRepository, times(1)).save(any(Todo.class));
        assertThat(existingTodo.getTitle()).isEqualTo("수정된 할 일");
        assertThat(existingTodo.getDescription()).isEqualTo("수정된 설명");
        assertThat(existingTodo.getDone()).isTrue();
    }

    @Test
    @DisplayName("할 일 삭제 테스트")
    void deleteTodoTest() {
        // Given
        UUID memberId = UUID.randomUUID();
        Long todoId = 1L;

        Todo todo = new Todo();
        todo.setId(todoId);

        when(todoRepository.findByIdAndMember_Id(todoId, memberId)).thenReturn(Optional.of(todo));
        doNothing().when(todoRepository).deleteById(todoId);

        // When
        todoService.deleteTodo(memberId, todoId);

        // Then
        verify(todoRepository, times(1)).findByIdAndMember_Id(todoId, memberId);
        verify(todoRepository, times(1)).deleteById(todoId);
    }

    @Test
    @DisplayName("할 일 ID로 조회 테스트")
    void getTodoByIdTest() {
        // Given
        Long todoId = 1L;

        Todo todo = new Todo();
        todo.setId(todoId);
        todo.setTitle("할 일");
        todo.setDescription("설명");
        todo.setDone(false);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        // When
        Todo result = todoService.getTodoById(todoId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(todoId);
        verify(todoRepository, times(1)).findById(todoId);
    }

    @Test
    @DisplayName("회원 및 할 일 ID로 할 일 조회 테스트")
    void getTodoByMemberIdAndTodoIdTest() {
        // Given
        UUID memberId = UUID.randomUUID();
        Long todoId = 1L;

        Todo todo = new Todo();
        todo.setId(todoId);
        todo.setTitle("할 일");
        todo.setDescription("설명");
        todo.setDone(false);

        when(todoRepository.findByIdAndMember_Id(todoId, memberId)).thenReturn(Optional.of(todo));

        // When
        Todo result = todoService.getTodoByMemberIdAndTodoId(memberId, todoId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(todoId);
        verify(todoRepository, times(1)).findByIdAndMember_Id(todoId, memberId);
    }
}
