package point.zzicback.todo.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.dto.request.CreateTodoRequest;
import point.zzicback.todo.domain.dto.request.UpdateTodoRequest;
import point.zzicback.todo.domain.dto.response.TodoMainResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    // Todo 엔티티를 TodoMainResponse로 변환
    TodoMainResponse toTodoMainResponse(Todo todo);

    // Todo 엔티티 리스트를 TodoMainResponse 리스트로 변환
    List<TodoMainResponse> toTodoMainResponseList(List<Todo> todos);

    // CreateTodoRequest를 Todo 엔티티로 변환
    Todo toTodo(CreateTodoRequest createTodoRequest);

    // UpdateTodoRequest와 ID를 사용하여 Todo 엔티티 생성
    @Mapping(target = "id", source = "id")
    Todo toTodo(UpdateTodoRequest updateTodoRequest, Long id);
}
