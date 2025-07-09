package point.ttodoApi.challenge.presentation.mapper;

import org.mapstruct.Mapper;
import point.ttodoApi.challenge.application.dto.result.ChallengeTodoResult;
import point.ttodoApi.challenge.presentation.dto.response.ChallengeTodoResponse;

@Mapper(componentModel = "spring")
public interface ChallengeTodoPresentationMapper {
    
    ChallengeTodoResponse toResponse(ChallengeTodoResult challengeTodoDto);
}