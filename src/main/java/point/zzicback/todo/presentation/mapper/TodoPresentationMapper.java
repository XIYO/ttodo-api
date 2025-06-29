package point.zzicback.todo.presentation.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.PageRequest;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.*;
import point.zzicback.todo.presentation.dto.*;

import java.util.*;

@Mapper(componentModel = "spring", imports = {PageRequest.class})
public interface TodoPresentationMapper {
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "priorityId", source = "request.priorityId")
  @Mapping(target = "tags", source = "request.tags")
  CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "todoId", source = "todoId")
  @Mapping(target = "statusId", source = "request.statusId")
  @Mapping(target = "priorityId", source = "request.priorityId")
  @Mapping(target = "tags", source = "request.tags")
  @Mapping(target = "originalTodoId", ignore = true)
  UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "pageable", expression = "java(PageRequest.of(request.page(), request.size()))")
  TodoSearchQuery toQuery(TodoSearchRequest request, UUID memberId);

  point.zzicback.todo.presentation.dto.TodoResponse toResponse(TodoResult todoResult);
  
  CalendarTodoStatusResponse toCalendarResponse(CalendarTodoStatus status);
  
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
