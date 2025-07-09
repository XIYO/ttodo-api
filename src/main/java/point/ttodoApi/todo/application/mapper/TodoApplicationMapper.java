package point.ttodoApi.todo.application.mapper;

import org.mapstruct.*;
import point.ttodoApi.common.config.MapStructConfig;
import point.ttodoApi.todo.application.dto.result.TodoResult;
import point.ttodoApi.todo.domain.*;

import java.time.LocalDate;

@Mapper(config = MapStructConfig.class)
public interface TodoApplicationMapper {

    // 공통 매핑 설정 - TodoOriginal의 기본 필드들을 위한 추상 메서드 (직접 호출되지 않음)
    @Mapping(target = "priorityName", source = "priorityId", qualifiedByName = "priorityName")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "priorityId", source = "priorityId")
    @Mapping(target = "tags", source = "tags")
    TodoResult toBaseResult(TodoOriginal todoOriginal);

    // 원본 TodoOriginal -> TodoResult
    @InheritConfiguration(name = "toBaseResult")
    @Mapping(target = "id", expression = "java(todoOriginal.getId() + \":0\")")
    @Mapping(target = "complete", source = "completed")
    @Mapping(target = "isPinned", source = "isPinned")
    @Mapping(target = "displayOrder", source = "displayOrder")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "time", source = "time")
    @Mapping(target = "originalTodoId", source = "id")
    TodoResult toResult(TodoOriginal todoOriginal);

    // 가상 TodoOriginal -> TodoResult
    @Mapping(target = "priorityName", source = "todoOriginal.priorityId", qualifiedByName = "priorityName")
    @Mapping(target = "categoryId", source = "todoOriginal.category.id")
    @Mapping(target = "categoryName", source = "todoOriginal.category.name")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "title", source = "todoOriginal.title")
    @Mapping(target = "description", source = "todoOriginal.description")
    @Mapping(target = "priorityId", source = "todoOriginal.priorityId")
    @Mapping(target = "tags", source = "todoOriginal.tags")
    @Mapping(target = "id", source = "virtualId")
    @Mapping(target = "complete", constant = "false")
    @Mapping(target = "isPinned", source = "todoOriginal.isPinned")
    @Mapping(target = "displayOrder", source = "todoOriginal.displayOrder")
    @Mapping(target = "date", source = "virtualDate")
    @Mapping(target = "time", source = "todoOriginal.time")
    @Mapping(target = "repeatType", source = "todoOriginal.repeatType")
    @Mapping(target = "repeatInterval", source = "todoOriginal.repeatInterval")
    @Mapping(target = "repeatEndDate", source = "todoOriginal.repeatEndDate")
    @Mapping(target = "daysOfWeek", source = "todoOriginal.daysOfWeek")
    @Mapping(target = "originalTodoId", source = "todoOriginal.id")
    TodoResult toVirtualResult(TodoOriginal todoOriginal, String virtualId, LocalDate virtualDate);

    // 원본 날짜를 가진 TodoOriginal -> TodoResult
    @Mapping(target = "priorityName", source = "todoOriginal.priorityId", qualifiedByName = "priorityName")
    @Mapping(target = "categoryId", source = "todoOriginal.category.id")
    @Mapping(target = "categoryName", source = "todoOriginal.category.name")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "title", source = "todoOriginal.title")
    @Mapping(target = "description", source = "todoOriginal.description")
    @Mapping(target = "priorityId", source = "todoOriginal.priorityId")
    @Mapping(target = "tags", source = "todoOriginal.tags")
    @Mapping(target = "id", source = "virtualId")
    @Mapping(target = "complete", source = "todoOriginal.completed")
    @Mapping(target = "isPinned", source = "todoOriginal.isPinned")
    @Mapping(target = "displayOrder", source = "todoOriginal.displayOrder")
    @Mapping(target = "date", source = "originalDate")
    @Mapping(target = "time", source = "todoOriginal.time")
    @Mapping(target = "repeatType", source = "todoOriginal.repeatType")
    @Mapping(target = "repeatInterval", source = "todoOriginal.repeatInterval")
    @Mapping(target = "repeatEndDate", source = "todoOriginal.repeatEndDate")
    @Mapping(target = "daysOfWeek", source = "todoOriginal.daysOfWeek")
    @Mapping(target = "originalTodoId", source = "todoOriginal.id")
    TodoResult toOriginalResult(TodoOriginal todoOriginal, String virtualId, LocalDate originalDate);

    // Todo 엔티티 -> TodoResult
    @Mapping(target = "id", expression = "java(todo.getTodoId().getId() + \":\" + todo.getTodoId().getSeq())")
    @Mapping(target = "priorityName", source = "priorityId", qualifiedByName = "priorityName")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "originalTodoId", expression = "java(todo.getTodoId().getId())")
    @Mapping(target = "repeatType", ignore = true)
    @Mapping(target = "repeatInterval", ignore = true)
    @Mapping(target = "repeatEndDate", ignore = true)
    @Mapping(target = "daysOfWeek", ignore = true)
    TodoResult toResult(Todo todo);

    @Named("priorityName")
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