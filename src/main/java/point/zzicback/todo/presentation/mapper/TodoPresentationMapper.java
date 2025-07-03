package point.zzicback.todo.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.PageRequest;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.*;
import point.zzicback.todo.presentation.dto.*;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {PageRequest.class})
public interface TodoPresentationMapper {
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "priorityId", source = "request.priorityId")
  @Mapping(target = "date", source = "request.date")
  @Mapping(target = "time", source = "request.time")
  @Mapping(target = "tags", source = "request.tags")
  CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "todoId", source = "todoId")
  @Mapping(target = "complete", source = "request.complete")
  @Mapping(target = "priorityId", source = "request.priorityId")
  @Mapping(target = "date", source = "request.date")
  @Mapping(target = "time", source = "request.time")
  @Mapping(target = "tags", source = "request.tags")
  @Mapping(target = "originalTodoId", ignore = true)
  UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  @Mapping(target = "virtualTodoId", source = "virtualId")
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "title", source = "request.title")
  @Mapping(target = "description", source = "request.description")
  @Mapping(target = "complete", source = "request.complete")
  @Mapping(target = "priorityId", source = "request.priorityId")
  @Mapping(target = "categoryId", source = "request.categoryId")
  @Mapping(target = "date", source = "request.date")
  @Mapping(target = "time", source = "request.time")
  @Mapping(target = "tags", source = "request.tags")
  UpdateVirtualTodoCommand toVirtualCommand(UpdateTodoRequest request, UUID memberId, String virtualId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "complete", source = "request.complete")
  @Mapping(target = "categoryIds", source = "request.categoryIds")
  @Mapping(target = "priorityIds", source = "request.priorityIds")
  @Mapping(target = "tags", source = "request.tags")
  @Mapping(target = "startDate", source = "request.startDate")
  @Mapping(target = "endDate", source = "request.endDate")
  @Mapping(target = "date", source = "request.date")
  @Mapping(target = "pageable", expression = "java(PageRequest.of(request.page(), request.size()))")
  TodoSearchQuery toQuery(TodoSearchRequest request, UUID memberId);

  point.zzicback.todo.presentation.dto.TodoResponse toResponse(TodoResult todoResult);

  default boolean isOnlyCompleteFieldUpdate(UpdateTodoRequest request) {
    return request.getComplete() != null &&
           (request.getTitle() == null || request.getTitle().trim().isEmpty()) &&
           (request.getDescription() == null || request.getDescription().trim().isEmpty()) &&
           request.getPriorityId() == null &&
           request.getCategoryId() == null &&
           request.getDate() == null &&
           request.getTime() == null &&
           request.getRepeatType() == null &&
           request.getRepeatInterval() == null &&
           request.getRepeatStartDate() == null &&
           request.getRepeatEndDate() == null &&
           (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()) &&
           (request.getTags() == null || request.getTags().isEmpty());
  }
}
