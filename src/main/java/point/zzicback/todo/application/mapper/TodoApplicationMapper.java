package point.zzicback.todo.application.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;

@Mapper(componentModel = "spring")
public interface TodoApplicationMapper {

    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "statusName", ignore = true)
    @Mapping(target = "priorityId", source = "priorityId")
    @Mapping(target = "priorityName", ignore = true)
    @Mapping(target = "categoryId", expression = "java(todo.getCategory() != null ? todo.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(todo.getCategory() != null ? todo.getCategory().getName() : null)")
    TodoResult toResult(Todo todo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "statusId", constant = "0")
    @Mapping(target = "priorityId", source = "priority")
    Todo toEntity(CreateTodoCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "statusId", source = "status")
    @Mapping(target = "priorityId", source = "priority")
    @Mapping(target = "category", ignore = true)
    void updateEntity(UpdateTodoCommand command, @MappingTarget Todo todo);
}
