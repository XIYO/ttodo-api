package point.zzicback.todo.application.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
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
