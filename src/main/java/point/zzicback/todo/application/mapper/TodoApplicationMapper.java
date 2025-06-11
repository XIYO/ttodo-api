package point.zzicback.todo.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.command.UpdateTodoCommand;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;

@Mapper(componentModel = "spring")
public interface TodoApplicationMapper {

    TodoResult toResult(Todo todo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "done", constant = "false")
    Todo toEntity(CreateTodoCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    void updateEntity(UpdateTodoCommand command, @MappingTarget Todo todo);
}
