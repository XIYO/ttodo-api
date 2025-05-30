package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import point.zzicback.member.persistance.MemberRepository;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.persistance.TodoRepository;

import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final MemberRepository memberRepository;

    public Page<Todo> getTodoListByMemberWithPagination(UUID memberId, Boolean done, Pageable pageable) {
        return done == null
                ? todoRepository.findByMemberId(memberId, pageable)
                : todoRepository.findByMemberIdAndDone(memberId, done, pageable);
    }

    public Todo getTodoByMemberIdAndTodoId(UUID memberId, Long todoId) {
        return todoRepository.findByIdAndMemberId(todoId, memberId).orElse(null);
    }

    public long createTodo(UUID memberId, Todo todo) {
        return memberRepository.findById(memberId)
                .map(member -> {
                    todo.setMember(member);
                    return todoRepository.save(todo).getId();
                })
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public void updateTodo(UUID memberId, Todo todo) {
        todoRepository.findByIdAndMemberId(todo.getId(), memberId).ifPresent(existingTodo -> {
            ofNullable(todo.getTitle()).ifPresent(existingTodo::setTitle);
            ofNullable(todo.getDescription()).ifPresent(existingTodo::setDescription);
            ofNullable(todo.getDone()).ifPresent(existingTodo::setDone);
            todoRepository.save(existingTodo);
        });
    }


    public void deleteTodo(UUID memberId, Long id) {
        todoRepository.findByIdAndMemberId(id, memberId)
                .ifPresent(todo -> todoRepository.deleteById(id));
    }
}
