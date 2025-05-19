package point.zzicback.todo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.persistance.TodoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

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
    @DisplayName("할 일 생성 테스트")
    void createTodoTest() {
        // Given
        Todo todo = new Todo();
        todo.setTitle("새로운 할 일");
        todo.setDescription("설명");
        todo.setDone(false);

        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        // When
        todoService.createTodo(todo);

        // Then
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    @DisplayName("할 일 수정 테스트")
    void updateTodoTest() {
        // Given
        Long id = 1L;
        Todo existingTodo = new Todo();
        existingTodo.setId(id);
        existingTodo.setTitle("기존 할 일");
        existingTodo.setDescription("기존 설명");
        existingTodo.setDone(false);

        Todo updatedTodo = new Todo();
        updatedTodo.setId(id);
        updatedTodo.setTitle("수정된 할 일");
        updatedTodo.setDescription("수정된 설명");
        updatedTodo.setDone(true);

        when(todoRepository.findById(id)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(existingTodo);

        // When
        todoService.updateTodo(updatedTodo);

        // Then
        verify(todoRepository, times(1)).findById(id);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("할 일 삭제 테스트")
    void deleteTodoTest() {
        // Given
        Long id = 1L;
        doNothing().when(todoRepository).deleteById(id);

        // When
        todoService.deleteTodo(id);

        // Then
        verify(todoRepository, times(1)).deleteById(id);
    }
}