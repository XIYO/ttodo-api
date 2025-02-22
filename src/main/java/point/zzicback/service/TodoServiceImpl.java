package point.zzicback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.domain.Todo;
import point.zzicback.repository.TodoRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;

    @Override
    public List<Todo> findAll() {
        return this.todoRepository.findAll();
    }

    @Override
    public Optional<Todo> findById(Long id) {
        return this.todoRepository.findById(id);
    }

    @Override
    public void save(Todo todo) {
        this.todoRepository.save(todo);
    }

    @Override
    public void deleteById(Long id) {
        this.todoRepository.deleteById(id);
    }
}
