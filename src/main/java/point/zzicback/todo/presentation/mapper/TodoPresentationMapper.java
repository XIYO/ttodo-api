package point.zzicback.todo.presentation.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.application.dto.result.TodoStatistics;
import point.zzicback.todo.presentation.dto.CreateTodoRequest;
import point.zzicback.todo.presentation.dto.UpdateTodoRequest;
import point.zzicback.todo.presentation.dto.TodoStatisticsResponse;

import java.util.*;

@Mapper(componentModel = "spring", imports = {Arrays.class, java.util.stream.Collectors.class})
public interface TodoPresentationMapper {
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "priority", source = "request.priorityId")
  @Mapping(target = "tags", expression = "java(parseTagsString(request.getTags()))")
  CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "todoId", source = "todoId")
  @Mapping(target = "status", source = "request.statusId")
  @Mapping(target = "priority", source = "request.priorityId")
  @Mapping(target = "tags", expression = "java(parseTagsString(request.getTags()))")
  UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  point.zzicback.todo.presentation.dto.TodoResponse toResponse(TodoResult todoResult);
  
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
