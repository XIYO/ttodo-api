package point.ttodoApi.todo.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import point.ttodoApi.shared.config.MapStructConfig;
import point.ttodoApi.todo.application.command.*;
import point.ttodoApi.todo.domain.TodoDefinition;
import point.ttodoApi.todo.domain.TodoInstance;
import point.ttodoApi.todo.domain.TodoView;
import point.ttodoApi.todo.presentation.dto.request.*;
import point.ttodoApi.todo.presentation.dto.response.*;

import java.util.UUID;

@Mapper(config = MapStructConfig.class, imports = {PageRequest.class}, componentModel = "spring", uses = {RecurrenceRuleMapper.class})
@SuppressWarnings("NullableProblems")
@Component
public abstract class TodoPresentationMapper {

  @org.springframework.beans.factory.annotation.Autowired
  private ObjectMapper objectMapper;

  @org.springframework.beans.factory.annotation.Autowired
  private RecurrenceRuleMapper recurrenceRuleMapper;

  // Old Todo mapping methods removed for new architecture

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

  // Instant to LocalDateTime 변환
  protected java.time.LocalDateTime map(java.time.Instant instant) {
    if (instant == null) return null;
    return java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
  }


  // TodoDefinition 매핑 메서드
  public abstract CreateTodoDefinitionCommand toCreateCommand(CreateTodoDefinitionRequest request);
  public abstract UpdateTodoDefinitionCommand toUpdateCommand(UpdateTodoDefinitionRequest request);

  @Mapping(target = "instanceCount", expression = "java((int)definition.getInstances().stream().filter(i -> i.getDeletedAt() == null).count())")
  @Mapping(target = "completedInstanceCount", expression = "java((int)definition.getInstances().stream().filter(i -> i.getDeletedAt() == null && i.isCompleted()).count())")
  @Mapping(target = "ownerNickname", ignore = true) // Set by service layer
  @Mapping(target = "categoryName", source = "category.name")
  @Mapping(target = "ownerId", source = "owner.id")
  public abstract TodoDefinitionResponse toDefinitionResponse(TodoDefinition definition);

  // TodoInstance 매핑 메서드
  public abstract CreateTodoInstanceCommand toCreateCommand(CreateTodoInstanceRequest request);
  public abstract UpdateTodoInstanceCommand toUpdateCommand(UpdateTodoInstanceRequest request);
  public abstract UpdateTodoStatusCommand toStatusCommand(UpdateTodoStatusRequest request);

  @Mapping(target = "definitionTitle", source = "definition.title")
  @Mapping(target = "definitionDescription", source = "definition.description")
  @Mapping(target = "definitionPriorityId", source = "definition.priorityId")
  @Mapping(target = "categoryName", source = "instance.category.name", defaultExpression = "java(instance.getDefinition().getCategory() != null ? instance.getDefinition().getCategory().getName() : null)")
  @Mapping(target = "isRecurring", expression = "java(instance.getDefinition().getRecurrenceRule() != null)")
  @Mapping(target = "completed", source = "completed")
  public abstract TodoInstanceResponse toInstanceResponse(TodoInstance instance);

  // TodoView 매핑 메서드
  @Mapping(target = "completed", source = "completed")
  @Mapping(target = "priorityName", expression = "java(getPriorityName(view.getPriorityId()))")
  public abstract TodoViewResponse toViewResponse(TodoView view);

  // 우선순위 ID를 우선순위명으로 변환
  protected String getPriorityName(Integer priorityId) {
    if (priorityId == null) return "보통";
    return switch (priorityId) {
      case 0 -> "낮음";
      case 1 -> "보통";
      case 2 -> "높음";
      default -> "알 수 없음";
    };
  }
}
