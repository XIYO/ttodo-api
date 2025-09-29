package point.ttodoApi.challenge.application.mapper;

import org.mapstruct.*;
import point.ttodoApi.shared.config.MapStructConfig;
import point.ttodoApi.challenge.application.result.ChallengeTodoResult;
import point.ttodoApi.challenge.domain.ChallengeTodo;

/**
 * MapStruct mapper for converting ChallengeTodo domain entities to application layer DTOs.
 */
@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface ChallengeTodoMapper {
  /**
   * Convert ChallengeTodo to ChallengeTodoResult.
   *
   * @param challengeTodo domain entity
   * @return application DTO
   */
  @Mapping(target = "challengeTitle", source = "challengeTodo.challengeParticipation.challenge.title")
  @Mapping(target = "challengeDescription", source = "challengeTodo.challengeParticipation.challenge.description")
  @Mapping(target = "startDate", source = "period.startDate")
  @Mapping(target = "endDate", source = "period.endDate")
  @Mapping(target = "isPersisted", expression = "java(challengeTodo.getId() != null)")
  @Mapping(target = "periodType", source = "challengeTodo.challengeParticipation.challenge.periodType")
  ChallengeTodoResult toResult(ChallengeTodo challengeTodo);
}
