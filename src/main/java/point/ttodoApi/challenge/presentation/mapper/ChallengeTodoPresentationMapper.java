package point.ttodoApi.challenge.presentation.mapper;

import org.mapstruct.Mapper;
import point.ttodoApi.challenge.application.result.ChallengeTodoResult;
import point.ttodoApi.challenge.presentation.dto.response.ChallengeTodoResponse;

@Mapper(componentModel = "spring")
@SuppressWarnings("NullableProblems")
public interface ChallengeTodoPresentationMapper {

  ChallengeTodoResponse toResponse(ChallengeTodoResult challengeTodoDto);
}