package point.zzicback.todo.presentation.mapper;

import org.mapstruct.*;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.application.dto.result.TodoStatistics;
import point.zzicback.todo.presentation.dto.CreateTodoRequest;
import point.zzicback.todo.presentation.dto.UpdateTodoRequest;
import point.zzicback.todo.presentation.dto.TodoStatisticsResponse;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TodoPresentationMapper {
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "priority", source = "request.priorityId")
  CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "todoId", source = "todoId")
  @Mapping(target = "status", source = "request.statusId")
  @Mapping(target = "priority", source = "request.priorityId")
  UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  point.zzicback.todo.presentation.dto.TodoResponse toResponse(TodoResult todoResult);
  
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
