package point.zzicback.experience.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.experience.application.dto.result.MemberLevelResult;
import point.zzicback.experience.presentation.dto.response.MemberLevelResponse;

@Mapper(componentModel = "spring")
public interface ExperiencePresentationMapper {
    
    @Mapping(target = "currentExp", source = "currentExp")
    @Mapping(target = "currentLevelMinExp", source = "currentLevelMinExp")
    MemberLevelResponse toResponse(MemberLevelResult result);
}
