package point.zzicback.todo.application.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;

@Mapper(componentModel = "spring", imports = {TodoId.class})
public interface TodoApplicationMapper {

    @Mapping(target = "id", expression = "java(todo.getVirtualId())")
    @Mapping(target = "complete", source = "complete")
    @Mapping(target = "priorityId", source = "priorityId")
    @Mapping(target = "priorityName", ignore = true)
    @Mapping(target = "categoryId", expression = "java(todo.getCategory() != null ? todo.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(todo.getCategory() != null ? todo.getCategory().getName() : null)")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "time", source = "time")
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
    @Mapping(target = "date", source = "command.date")
    @Mapping(target = "time", source = "command.time")
    @Mapping(target = "tags", source = "command.tags")
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "complete", constant = "true")
    @Mapping(target = "category", ignore = true)
    Todo toEntity(CreateTodoCommand command, Long originalTodoId, Long daysDifference);

    @Mapping(target = "todoId", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "complete", source = "complete")
    @Mapping(target = "priorityId", source = "priorityId")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "time", source = "time")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateTodoCommand command, @MappingTarget Todo todo);
}
