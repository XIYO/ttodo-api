package point.zzicback.todo.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.command.UpdateTodoCommand;
import point.zzicback.todo.application.dto.response.TodoResponse;
import point.zzicback.todo.presentation.dto.CreateTodoRequest;
import point.zzicback.todo.presentation.dto.UpdateTodoRequest;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TodoPresentationMapper {

    @Mapping(target = "memberId", source = "memberId")
    CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "todoId", source = "todoId")
    UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

    point.zzicback.todo.presentation.dto.TodoResponse toResponse(TodoResponse todoResponse);
}
