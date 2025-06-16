package point.zzicback.challenge.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.challenge.application.dto.result.ChallengeTodoResult;
import point.zzicback.challenge.domain.ChallengeTodo;

/**
 * MapStruct mapper for converting ChallengeTodo domain entities to application layer DTOs.
 */
@Mapper(componentModel = "spring")
public interface ChallengeTodoMapper {
    /**
     * Convert ChallengeTodo to ChallengeTodoResult.
     * @param challengeTodo domain entity
     * @return application DTO
     */
    @Mapping(target = "challengeTitle", source = "challengeTodo.challengeParticipation.challenge.title")
    @Mapping(target = "challengeDescription", source = "challengeTodo.challengeParticipation.challenge.description")
    @Mapping(target = "startDate", source = "period.startDate")
    @Mapping(target = "endDate", source = "period.endDate")
    @Mapping(target = "done", source = "done")
    @Mapping(target = "isPersisted", expression = "java(challengeTodo.getId() != null)")
    @Mapping(target = "periodType", source = "challengeTodo.challengeParticipation.challenge.periodType")
    ChallengeTodoResult toResult(ChallengeTodo challengeTodo);
}