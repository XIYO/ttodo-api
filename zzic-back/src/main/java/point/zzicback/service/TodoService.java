package point.zzicback.service;

import point.zzicback.model.Todo;

import java.util.List;

/**
 * 인터페이스 입니다 구현체를 만들어주세요
 */
public interface TodoService {
    List<Todo> getTodoList();

    Todo getTodoById(Long id);

    void createTodo(Todo todo);

    void updateTodo(Todo todo);

    void deleteTodo(Long id);
}
