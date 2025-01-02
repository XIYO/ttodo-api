package point.zzicback.service;

import org.springframework.stereotype.Service;
import point.zzicback.model.Todo;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    @Override
    public List<Todo> getTodoList() {
        return List.of(); // 임시로 빈 리스트 반환
    }

    @Override
    public Todo getTodoById(Long id) {
        // 임시로 null 반환
        return null;
    }

    @Override
    public void createTodo(Todo todo) {
        // 임시로 아무 작업도 하지 않음
    }

    @Override
    public void updateTodo(Todo todo) {
        // 임시로 아무 작업도 하지 않음
    }

    @Override
    public void deleteTodo(Long id) {
        // 임시로 아무 작업도 하지 않음
    }
}
