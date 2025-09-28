package point.ttodoApi.experience.presentation.mapper;

import org.mapstruct.Mapper;
import point.ttodoApi.experience.application.result.UserLevelResult;
import point.ttodoApi.experience.presentation.dto.response.UserLevelResponse;
import point.ttodoApi.shared.config.shared.MapStructConfig;

@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface ExperiencePresentationMapper {

  // 동일한 필드명은 자동 매핑되므로 @Mapping 불필요
  UserLevelResponse toResponse(UserLevelResult result);
}
