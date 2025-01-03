package point.zzicback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.mapper.TodoMapper;
import point.zzicback.model.Todo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;

    @Override
    public List<Todo> getAll() {
        return this.todoMapper.selectAll();
    }

    @Override
    public Todo getById(Long id) {
        return this.todoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int add(Todo todo) {
        return this.todoMapper.insert(todo);
    }

    @Override
    public int modify(Todo todo) {
        return this.todoMapper.updateByPrimaryKeySelective(todo);
    }

    @Override
    public int remove(Long id) {
        return this.todoMapper.deleteByPrimaryKey(id);
    }
}
