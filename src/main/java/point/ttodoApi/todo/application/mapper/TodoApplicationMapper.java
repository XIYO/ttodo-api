package point.ttodoApi.todo.application.mapper;

import org.mapstruct.*;
import point.ttodoApi.shared.config.MapStructConfig;
import point.ttodoApi.todo.application.result.TodoResult;
import point.ttodoApi.todo.domain.*;

import java.time.LocalDate;

@Mapper(config = MapStructConfig.class)
public interface TodoApplicationMapper {

  // 공통 매핑 설정 - TodoTemplate의 기본 필드들을 위한 추상 메서드 (직접 호출되지 않음)
  @Mapping(target = "priorityName", source = "priorityId", qualifiedByName = "priorityName")
  @Mapping(target = "categoryId", source = "category.id")
  @Mapping(target = "categoryName", source = "category.name")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "title", source = "title")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "priorityId", source = "priorityId")
  @Mapping(target = "tags", source = "tags")
  TodoResult toBaseResult(TodoTemplate todoTemplate);

  // 원본 TodoTemplate -> TodoResult
  @InheritConfiguration(name = "toBaseResult")
  @Mapping(target = "id", expression = "java(todoTemplate.getId() + \":0\")")
  @Mapping(target = "complete", source = "complete")
  @Mapping(target = "isPinned", source = "isPinned")
  @Mapping(target = "displayOrder", source = "displayOrder")
  @Mapping(target = "date", source = "date")
  @Mapping(target = "time", source = "time")
  @Mapping(target = "recurrenceRule", source = "recurrenceRule")
  @Mapping(target = "anchorDate", source = "anchorDate")
  @Mapping(target = "originalTodoId", source = "id")
  TodoResult toResult(TodoTemplate todoTemplate);

  // 가상 TodoTemplate -> TodoResult
  @Mapping(target = "priorityName", source = "todoTemplate.priorityId", qualifiedByName = "priorityName")
  @Mapping(target = "categoryId", source = "todoTemplate.category.id")
  @Mapping(target = "categoryName", source = "todoTemplate.category.name")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "title", source = "todoTemplate.title")
  @Mapping(target = "description", source = "todoTemplate.description")
  @Mapping(target = "priorityId", source = "todoTemplate.priorityId")
  @Mapping(target = "tags", source = "todoTemplate.tags")
  @Mapping(target = "id", source = "virtualId")
  @Mapping(target = "complete", constant = "false")
  @Mapping(target = "isPinned", source = "todoTemplate.isPinned")
  @Mapping(target = "displayOrder", source = "todoTemplate.displayOrder")
  @Mapping(target = "date", source = "virtualDate")
  @Mapping(target = "time", source = "todoTemplate.time")
  @Mapping(target = "recurrenceRule", source = "todoTemplate.recurrenceRule")
  @Mapping(target = "anchorDate", source = "todoTemplate.anchorDate")
  @Mapping(target = "originalTodoId", source = "todoTemplate.id")
  TodoResult toVirtualResult(TodoTemplate todoTemplate, String virtualId, LocalDate virtualDate);

  // 원본 날짜를 가진 TodoTemplate -> TodoResult
  @Mapping(target = "priorityName", source = "todoTemplate.priorityId", qualifiedByName = "priorityName")
  @Mapping(target = "categoryId", source = "todoTemplate.category.id")
  @Mapping(target = "categoryName", source = "todoTemplate.category.name")
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "title", source = "todoTemplate.title")
  @Mapping(target = "description", source = "todoTemplate.description")
  @Mapping(target = "priorityId", source = "todoTemplate.priorityId")
  @Mapping(target = "tags", source = "todoTemplate.tags")
  @Mapping(target = "id", source = "virtualId")
  @Mapping(target = "complete", source = "todoTemplate.complete")
  @Mapping(target = "isPinned", source = "todoTemplate.isPinned")
  @Mapping(target = "displayOrder", source = "todoTemplate.displayOrder")
  @Mapping(target = "date", source = "originalDate")
  @Mapping(target = "time", source = "todoTemplate.time")
  @Mapping(target = "recurrenceRule", source = "todoTemplate.recurrenceRule")
  @Mapping(target = "anchorDate", source = "todoTemplate.anchorDate")
  @Mapping(target = "originalTodoId", source = "todoTemplate.id")
  TodoResult toOriginalResult(TodoTemplate todoTemplate, String virtualId, LocalDate originalDate);

  // Todo 엔티티 -> TodoResult
  @Mapping(target = "id", expression = "java(todo.getTodoId().getId() + \":\" + todo.getTodoId().getSeq())")
  @Mapping(target = "priorityName", source = "priorityId", qualifiedByName = "priorityName")
  @Mapping(target = "categoryId", source = "category.id")
  @Mapping(target = "categoryName", source = "category.name")
  @Mapping(target = "originalTodoId", expression = "java(todo.getTodoId().getId())")
  @Mapping(target = "recurrenceRule", ignore = true)
  @Mapping(target = "anchorDate", ignore = true)
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