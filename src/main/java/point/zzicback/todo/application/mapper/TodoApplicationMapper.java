package point.zzicback.todo.application.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoOriginal;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface TodoApplicationMapper {

    @Mapping(target = "id", expression = "java(todoOriginal.getId() + \":0\")")
    @Mapping(target = "complete", source = "completed")
    @Mapping(target = "isPinned", source = "isPinned")
    @Mapping(target = "displayOrder", source = "displayOrder")
    @Mapping(target = "priorityName", expression = "java(getPriorityName(todoOriginal.getPriorityId()))")
    @Mapping(target = "categoryId", expression = "java(todoOriginal.getCategory() != null ? todoOriginal.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(todoOriginal.getCategory() != null ? todoOriginal.getCategory().getName() : null)")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "time", source = "time")
    @Mapping(target = "originalTodoId", source = "id")
    TodoResult toResult(TodoOriginal todoOriginal);

    @Mapping(target = "id", source = "virtualId")
    @Mapping(target = "complete", constant = "false")
    @Mapping(target = "isPinned", source = "todoOriginal.isPinned")
    @Mapping(target = "displayOrder", source = "todoOriginal.displayOrder")
    @Mapping(target = "priorityName", expression = "java(getPriorityName(todoOriginal.getPriorityId()))")
    @Mapping(target = "categoryId", expression = "java(todoOriginal.getCategory() != null ? todoOriginal.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(todoOriginal.getCategory() != null ? todoOriginal.getCategory().getName() : null)")
    @Mapping(target = "date", source = "virtualDate")
    @Mapping(target = "time", source = "todoOriginal.time")
    @Mapping(target = "repeatType", source = "todoOriginal.repeatType")
    @Mapping(target = "repeatInterval", source = "todoOriginal.repeatInterval")
    @Mapping(target = "repeatEndDate", source = "todoOriginal.repeatEndDate")
    @Mapping(target = "daysOfWeek", source = "todoOriginal.daysOfWeek")
    @Mapping(target = "originalTodoId", source = "todoOriginal.id")
    @Mapping(target = "tags", source = "todoOriginal.tags")
    TodoResult toVirtualResult(TodoOriginal todoOriginal, String virtualId, LocalDate virtualDate);

    @Mapping(target = "id", source = "virtualId")
    @Mapping(target = "complete", source = "todoOriginal.completed")
    @Mapping(target = "isPinned", source = "todoOriginal.isPinned")
    @Mapping(target = "displayOrder", source = "todoOriginal.displayOrder")
    @Mapping(target = "priorityName", expression = "java(getPriorityName(todoOriginal.getPriorityId()))")
    @Mapping(target = "categoryId", expression = "java(todoOriginal.getCategory() != null ? todoOriginal.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(todoOriginal.getCategory() != null ? todoOriginal.getCategory().getName() : null)")
    @Mapping(target = "date", source = "originalDate")
    @Mapping(target = "time", source = "todoOriginal.time")
    @Mapping(target = "repeatType", source = "todoOriginal.repeatType")
    @Mapping(target = "repeatInterval", source = "todoOriginal.repeatInterval")
    @Mapping(target = "repeatEndDate", source = "todoOriginal.repeatEndDate")
    @Mapping(target = "daysOfWeek", source = "todoOriginal.daysOfWeek")
    @Mapping(target = "originalTodoId", source = "todoOriginal.id")
    @Mapping(target = "tags", source = "todoOriginal.tags")
    TodoResult toOriginalResult(TodoOriginal todoOriginal, String virtualId, LocalDate originalDate);

    @Mapping(target = "id", expression = "java(todo.getTodoId().getId() + \":\" + todo.getTodoId().getSeq())")
    @Mapping(target = "isPinned", source = "isPinned")
    @Mapping(target = "displayOrder", source = "displayOrder")
    @Mapping(target = "priorityName", expression = "java(getPriorityName(todo.getPriorityId()))")
    @Mapping(target = "categoryId", expression = "java(todo.getCategory() != null ? todo.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(todo.getCategory() != null ? todo.getCategory().getName() : null)")
    @Mapping(target = "originalTodoId", expression = "java(todo.getTodoId().getId())")
    @Mapping(target = "repeatType", ignore = true)
    @Mapping(target = "repeatInterval", ignore = true)
    @Mapping(target = "repeatEndDate", ignore = true)
    @Mapping(target = "daysOfWeek", ignore = true)
    TodoResult toResult(Todo todo);

    default String getPriorityName(Integer priorityId) {
        if (priorityId == null) return null;
        return switch (priorityId) {
            case 0 -> "낮음";
            case 1 -> "보통";
            case 2 -> "높음";
            default -> "알 수 없음";
        };
    }
}
