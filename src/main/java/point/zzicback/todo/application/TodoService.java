package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.query.MemberQuery;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.command.UpdateTodoCommand;
import point.zzicback.todo.application.dto.query.TodoListQuery;
import point.zzicback.todo.application.dto.query.TodoQuery;
import point.zzicback.todo.application.dto.response.TodoResponse;
import point.zzicback.todo.application.mapper.TodoApplicationMapper;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.persistance.TodoRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final MemberService memberService;
    private final TodoApplicationMapper todoApplicationMapper;

    public Page<TodoResponse> getTodoList(TodoListQuery query) {
        Page<Todo> todoPage = todoRepository.findByMemberIdAndDone(query.memberId(), query.done(), query.pageable());

        return todoPage.map(todoApplicationMapper::toResponse);
    }

    public TodoResponse getTodo(TodoQuery query) {
        return todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
                .map(todoApplicationMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Todo", query.todoId()));
    }

    @Transactional
    public void createTodo(CreateTodoCommand command) {
        MemberQuery memberQuery = new MemberQuery(command.memberId());
        var member = memberService.findVerifiedMember(memberQuery);
        Todo todo = todoApplicationMapper.toEntity(command);
        todo.setMember(member);
        todoRepository.save(todo);
    }

    @Transactional
    public void updateTodo(UpdateTodoCommand command) {
        Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
                .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
        todoApplicationMapper.updateEntity(command, todo);
    }

    @Transactional
    public void deleteTodo(TodoQuery query) {
        todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
                .ifPresentOrElse(todo -> todoRepository.deleteById(query.todoId()), () -> {
                    throw new EntityNotFoundException("Todo", query.todoId());
                });
    }
}
