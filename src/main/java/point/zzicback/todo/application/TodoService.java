package point.zzicback.todo.application;

import point.zzicback.todo.domain.Todo;

import java.util.List;
import java.util.UUID;

/**
 * 인터페이스 입니다 구현체를 만들어주세요
 */
public interface TodoService {
    List<Todo> getTodoList(Boolean done);

    Todo getTodoById(Long id);

    void createTodo(UUID memberId, Todo todo);

    void updateTodo(UUID memberId, Todo todo);

    void deleteTodo(UUID memberId,Long id);

    List<Todo> getTodoListByMember(UUID memberId, Boolean done);

    Todo getTodoByMemberIdAndTodoId(UUID memberId, Long todoId);
}
