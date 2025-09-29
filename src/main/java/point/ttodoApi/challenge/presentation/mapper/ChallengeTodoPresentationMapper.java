package point.ttodoApi.challenge.presentation.mapper;

import org.mapstruct.Mapper;
import point.ttodoApi.shared.config.MapStructConfig;
import point.ttodoApi.challenge.application.result.ChallengeTodoResult;
import point.ttodoApi.challenge.presentation.dto.response.ChallengeTodoResponse;

@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface ChallengeTodoPresentationMapper {

  ChallengeTodoResponse toResponse(ChallengeTodoResult challengeTodoDto);
}
