package point.zzicback.todo.presentation.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.PageRequest;
import point.zzicback.common.config.MapStructConfig;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.*;
import point.zzicback.todo.presentation.dto.request.*;
import point.zzicback.todo.presentation.dto.response.*;

import java.util.UUID;

@Mapper(config = MapStructConfig.class, imports = {PageRequest.class})
public interface TodoPresentationMapper {
  
  // Create 관련 매핑
  CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  // Update 관련 매핑
  @Mapping(target = "todoId", source = "todoId")
  @Mapping(target = "originalTodoId", ignore = true)
  UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  // Virtual Todo Update 매핑
  @Mapping(target = "virtualTodoId", source = "virtualId")
  UpdateVirtualTodoCommand toVirtualCommand(UpdateTodoRequest request, UUID memberId, String virtualId);

  // Search 관련 매핑
  @Mapping(target = "pageable", expression = "java(PageRequest.of(request.page() != null ? request.page() : 0, request.size() != null ? request.size() : 10))")
  TodoSearchQuery toQuery(TodoSearchRequest request, UUID memberId);

  // Response 매핑
  TodoResponse toResponse(TodoResult todoResult);

  // 헬퍼 메서드
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