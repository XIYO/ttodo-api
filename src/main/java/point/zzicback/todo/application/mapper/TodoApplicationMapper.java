package point.zzicback.todo.application.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.response.TodoResponse;
import point.zzicback.todo.domain.Todo;

@Mapper(componentModel = "spring")
public interface TodoApplicationMapper {
@Mapping(target = "id", ignore = true)
@Mapping(target = "member", ignore = true)
@Mapping(target = "done", constant = "false")
Todo toEntity(CreateTodoCommand command);

TodoResponse toResponse(Todo todo);

@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Mapping(target = "id", ignore = true)
@Mapping(target = "member", ignore = true)
void updateEntity(UpdateTodoCommand command, @MappingTarget Todo todo);
}
