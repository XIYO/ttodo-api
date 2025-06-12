package point.zzicback.challenge.presentation.mapper;

import org.mapstruct.Mapper;
import point.zzicback.challenge.application.dto.result.ChallengeTodoDto;
import point.zzicback.challenge.presentation.dto.ChallengeTodoResponse;

@Mapper(componentModel = "spring")
public interface ChallengeTodoPresentationMapper {
    
    ChallengeTodoResponse toResponse(ChallengeTodoDto challengeTodoDto);
}