package point.zzicback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.model.Todo;
import point.zzicback.repository.TodoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;

    @Override
    public List<Todo> getTodoList(Boolean done) {
        return todoRepository.findByDone(done);
    }

    @Override
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id).orElse(null);
    }

    @Override
    public void createTodo(Todo todo) {
        todoRepository.save(todo);
    }

    @Override
    public void updateTodo(Todo todo) {
        Todo existingTodo = todoRepository.findById(todo.getId()).orElse(null);
        if (existingTodo != null) {
            if (todo.getTitle() != null) {
                existingTodo.setTitle(todo.getTitle());
            }
            if (todo.getDescription() != null) {
                existingTodo.setDescription(todo.getDescription());
            }
            if (todo.getDone() != null) {
                existingTodo.setDone(todo.getDone());
            }
            todoRepository.save(existingTodo);
        }
    }

    @Override
    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }
}
