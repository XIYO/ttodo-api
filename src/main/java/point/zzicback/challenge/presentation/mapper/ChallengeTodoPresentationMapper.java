package point.zzicback.challenge.presentation.mapper;

import org.mapstruct.Mapper;
import point.zzicback.challenge.application.dto.result.ChallengeTodoResult;
import point.zzicback.challenge.presentation.dto.response.ChallengeTodoResponse;

@Mapper(componentModel = "spring")
public interface ChallengeTodoPresentationMapper {
    
    ChallengeTodoResponse toResponse(ChallengeTodoResult challengeTodoDto);
}