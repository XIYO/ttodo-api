package point.zzicback.todo.application.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;

@Mapper(componentModel = "spring", imports = {TodoId.class})
public interface TodoApplicationMapper {

    @Mapping(target = "id", expression = "java(todo.getVirtualId())")
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "statusName", ignore = true)
    @Mapping(target = "priorityId", source = "priorityId")
    @Mapping(target = "priorityName", ignore = true)
    @Mapping(target = "categoryId", expression = "java(todo.getCategory() != null ? todo.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(todo.getCategory() != null ? todo.getCategory().getName() : null)")
    @Mapping(target = "repeatType", ignore = true)
    @Mapping(target = "repeatInterval", ignore = true)
    @Mapping(target = "repeatEndDate", ignore = true)
    @Mapping(target = "daysOfWeek", ignore = true)
    @Mapping(target = "originalTodoId", expression = "java(todo.getOriginalTodoId())")
    TodoResult toResult(Todo todo);

    @Mapping(target = "todoId", expression = "java(new TodoId(originalTodoId, daysDifference))")
    @Mapping(target = "title", source = "command.title")
    @Mapping(target = "description", source = "command.description")
    @Mapping(target = "priorityId", source = "command.priorityId")
    @Mapping(target = "dueDate", source = "command.dueDate")
    @Mapping(target = "dueTime", source = "command.dueTime")
    @Mapping(target = "tags", source = "command.tags")
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "statusId", constant = "1")
    @Mapping(target = "category", ignore = true)
    Todo toEntity(CreateTodoCommand command, Long originalTodoId, Long daysDifference);

    @Mapping(target = "todoId", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "statusId", source = "statusId")
    @Mapping(target = "priorityId", source = "priorityId")
    @Mapping(target = "category", ignore = true)
    void updateEntity(UpdateTodoCommand command, @MappingTarget Todo todo);
}
