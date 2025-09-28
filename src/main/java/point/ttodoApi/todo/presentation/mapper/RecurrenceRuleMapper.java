package point.ttodoApi.todo.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.shared.config.shared.MapStructConfig;
import point.ttodoApi.todo.domain.recurrence.*;
import point.ttodoApi.todo.presentation.dto.request.*;

@Mapper(
        componentModel = "spring",
        config = MapStructConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@SuppressWarnings("NullableProblems")
public interface RecurrenceRuleMapper {

  @Mapping(target = "interval", expression = "java(request.interval() != null ? request.interval() : 1)")
  RecurrenceRule toDomain(RecurrenceRuleRequest request);

  EndCondition toDomain(EndConditionRequest request);
}

