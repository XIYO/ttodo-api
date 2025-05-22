package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.persistance.TodoRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<Todo> getTodoList(Boolean done) {
        return todoRepository.findByDone(done);
    }

    @Override
    public List<Todo> getTodoListByMember(UUID memberId, Boolean done) {
        return todoRepository.findByMemberIdAndDone(memberId, done);
    }
    @Override
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id).orElse(null);
    }

    @Override
    public Todo getTodoByMemberIdAndTodoId(UUID memberId, Long todoId) {
        return todoRepository.findByIdAndMember_Id(todoId, memberId).orElse(null);
    }

    @Override
    public void createTodo(UUID memberId, Todo todo) {
        Member member = memberRepository.findById(memberId).orElse(null);

        todo.setMember(member);
        todoRepository.save(todo);
    }

    @Override
    public void updateTodo(UUID memberId, Todo todo) {
        Todo existingTodo = todoRepository.findByIdAndMember_Id(todo.getId(),memberId).orElse(null);

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
    public void deleteTodo(UUID memberId, Long id) {
        Todo todo = todoRepository.findByIdAndMember_Id(id, memberId).orElse(null);
        todoRepository.deleteById(id);
    }
}
