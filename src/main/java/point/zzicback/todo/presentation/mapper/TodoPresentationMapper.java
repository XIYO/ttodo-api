package point.zzicback.todo.presentation.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.presentation.dto.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TodoPresentationMapper {
  @Mapping(target = "memberId", source = "memberId")
  CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "todoId", source = "todoId")
  UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  point.zzicback.todo.presentation.dto.TodoResponse toResponse(TodoResult todoResult);
}
