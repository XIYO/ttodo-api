package point.ttodoApi.todo.presentation.mapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.config.MapStructConfig;
import point.ttodoApi.todo.application.dto.command.*;
import point.ttodoApi.todo.application.dto.query.TodoSearchQuery;
import point.ttodoApi.todo.application.dto.result.TodoResult;
import point.ttodoApi.todo.domain.Todo;
import point.ttodoApi.todo.presentation.dto.request.*;
import point.ttodoApi.todo.presentation.dto.response.TodoResponse;

import java.util.UUID;

@Mapper(config = MapStructConfig.class, imports = {PageRequest.class}, componentModel = "spring", uses = {RecurrenceRuleMapper.class})
@Component
public abstract class TodoPresentationMapper {
  
  @org.springframework.beans.factory.annotation.Autowired
  private ObjectMapper objectMapper;

  @org.springframework.beans.factory.annotation.Autowired
  private RecurrenceRuleMapper recurrenceRuleMapper;

  // Create 관련 매핑
  @Mapping(target = "recurrenceRule", expression = "java(parseRecurrenceRule(request.recurrenceRuleJson()))")
  public abstract CreateTodoCommand toCommand(CreateTodoRequest request, UUID memberId);

  // Update 관련 매핑
  @Mapping(target = "todoId", source = "todoId")
  @Mapping(target = "recurrenceRule", expression = "java(parseRecurrenceRule(request.recurrenceRuleJson()))")
  @Mapping(target = "originalTodoId", ignore = true)
  public abstract UpdateTodoCommand toCommand(UpdateTodoRequest request, UUID memberId, Long todoId);

  // Virtual Todo Update 매핑
  @Mapping(target = "virtualTodoId", source = "virtualId")
  public abstract UpdateVirtualTodoCommand toVirtualCommand(UpdateTodoRequest request, UUID memberId, String virtualId);

  // Search 관련 매핑
  @Mapping(target = "pageable", expression = "java(PageRequest.of(request.getPage() != null ? request.getPage() : 0, request.getSize() != null ? request.getSize() : 10))")
  @Mapping(target = "date", ignore = true)
  @Mapping(target = "startDate", expression = "java(request.getStartDate())")
  @Mapping(target = "endDate", expression = "java(request.getEndDate())")
  @Mapping(target = "tags", ignore = true)
  public abstract TodoSearchQuery toQuery(TodoSearchRequest request, UUID memberId);
  
  // dates 처리 헬퍼 메서드들
  protected java.time.LocalDate processSingleDate(java.util.List<java.time.LocalDate> dates) {
    if (dates == null || dates.isEmpty()) return null;
    return dates.size() == 1 ? dates.get(0) : null;
  }
  
  protected java.time.LocalDate processStartDate(java.util.List<java.time.LocalDate> dates) {
    if (dates == null || dates.isEmpty()) return null;
    if (dates.size() == 1) return dates.get(0);  // 단일 날짜일 때도 startDate로 설정
    if (dates.size() == 2) return dates.get(0);
    return java.util.Collections.min(dates);
  }
  
  protected java.time.LocalDate processEndDate(java.util.List<java.time.LocalDate> dates) {
    if (dates == null || dates.isEmpty()) return null;
    if (dates.size() == 1) return dates.get(0);  // 단일 날짜일 때도 endDate로 설정
    if (dates.size() == 2) return dates.get(1);
    return java.util.Collections.max(dates);
  }

  // Response 매핑
  public abstract TodoResponse toResponse(TodoResult todoResult);
  
  // Domain Entity to Response 매핑
  public abstract TodoResponse toResponse(Todo todo);

  // JSON 문자열을 RecurrenceRule로 파싱
  protected point.ttodoApi.todo.domain.recurrence.RecurrenceRule parseRecurrenceRule(String json) {
    if (json == null || json.trim().isEmpty()) {
      return null;
    }
    try {
      RecurrenceRuleRequest req = objectMapper.readValue(json, RecurrenceRuleRequest.class);
      return recurrenceRuleMapper.toDomain(req);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid recurrence rule JSON: " + e.getMessage(), e);
    }
  }

  // 헬퍼 메서드
  public boolean isOnlyCompleteFieldUpdate(UpdateTodoRequest request) {
    return request.complete() != null &&
           (request.title() == null || request.title().trim().isEmpty()) &&
           (request.description() == null || request.description().trim().isEmpty()) &&
           request.priorityId() == null &&
           request.categoryId() == null &&
           request.date() == null &&
           request.time() == null &&
           request.recurrenceRuleJson() == null &&
           (request.tags() == null || request.tags().isEmpty());
  }
}
