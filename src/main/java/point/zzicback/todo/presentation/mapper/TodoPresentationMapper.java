package point.zzicback.todo.presentation.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.PageRequest;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.*;
import point.zzicback.todo.presentation.dto.*;

import java.util.*;

@Mapper(componentModel = "spring", imports = {Arrays.class, java.util.stream.Collectors.class, PageRequest.class})
public interface TodoPresentationMapper {
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "priorityId", source = "request.priorityId")
  @Mapping(target = "tags", expression = "java(parseTagsString(request.getTags()))")
  CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "todoId", source = "todoId")
  @Mapping(target = "statusId", source = "request.statusId")
  @Mapping(target = "priorityId", source = "request.priorityId")
  @Mapping(target = "tags", expression = "java(parseTagsString(request.getTags()))")
  @Mapping(target = "originalTodoId", ignore = true)
  UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "pageable", expression = "java(PageRequest.of(request.page(), request.size()))")
  TodoSearchQuery toQuery(TodoSearchRequest request, UUID memberId);

  point.zzicback.todo.presentation.dto.TodoResponse toResponse(TodoResult todoResult);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "originalTodoId", source = "originalTodoId")
  @Mapping(target = "completionDate", source = "request.completionDate")
  CompleteVirtualTodoCommand toCompleteVirtualTodoCommand(UUID memberId, Long originalTodoId, CompleteVirtualTodoRequest request);
  
  default Set<String> parseTagsString(String tagsString) {
    if (tagsString == null || tagsString.trim().isEmpty()) {
      return null;
    }
    return Arrays.stream(tagsString.split(","))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .collect(java.util.stream.Collectors.toSet());
  }
  
  default TodoStatisticsResponse toStatisticsResponse(TodoStatistics statistics) {
    List<TodoStatisticsResponse.StatisticsItem> content = List.of(
        new TodoStatisticsResponse.StatisticsItem("진행중", statistics.inProgress()),
        new TodoStatisticsResponse.StatisticsItem("기간초과", statistics.overdue()),
        new TodoStatisticsResponse.StatisticsItem("완료", statistics.completed()),
        new TodoStatisticsResponse.StatisticsItem("전체", statistics.total())
    );
    
    return new TodoStatisticsResponse(content);
  }
}
