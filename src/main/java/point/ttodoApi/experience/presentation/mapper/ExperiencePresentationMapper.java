package point.ttodoApi.experience.presentation.mapper;

import org.mapstruct.Mapper;
import point.ttodoApi.common.config.MapStructConfig;
import point.ttodoApi.experience.application.dto.result.MemberLevelResult;
import point.ttodoApi.experience.presentation.dto.response.MemberLevelResponse;

@Mapper(config = MapStructConfig.class)
public interface ExperiencePresentationMapper {
    
    // 동일한 필드명은 자동 매핑되므로 @Mapping 불필요
    MemberLevelResponse toResponse(MemberLevelResult result);
}
