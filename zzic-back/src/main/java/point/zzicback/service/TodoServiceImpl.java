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
    public List<Todo> getTodoList(Boolean isDone) {

        return this.todoMapper.selectAll(isDone);
    }

    @Override
    public Todo getTodoById(Long id) {
        // 임시로 null 반환
        return this.todoMapper.selectByPrimaryKey(id);
    }

    @Override
    public void createTodo(Todo todo) {

        this.todoMapper.insertSelective(todo);

    }

    @Override
    public void updateTodo(Todo todo) {
        this.todoMapper.updateByPrimaryKeySelective(todo);

    }

    @Override
    public void deleteTodo(Long id) {
        this.todoMapper.deleteByPrimaryKey(id);
    }
}
