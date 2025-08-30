package point.ttodoApi.todo.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import point.ttodoApi.common.config.MapStructConfig;
import point.ttodoApi.todo.domain.recurrence.EndCondition;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;
import point.ttodoApi.todo.presentation.dto.request.EndConditionRequest;
import point.ttodoApi.todo.presentation.dto.request.RecurrenceRuleRequest;

@Mapper(
    componentModel = "spring",
    config = MapStructConfig.class,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RecurrenceRuleMapper {

    @Mapping(target = "interval", expression = "java(request.interval() != null ? request.interval() : 1)")
    RecurrenceRule toDomain(RecurrenceRuleRequest request);

    EndCondition toDomain(EndConditionRequest request);
}

