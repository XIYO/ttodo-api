package point.ttodoApi.profile.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.shared.config.MapStructConfig;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Statistics;
import point.ttodoApi.profile.presentation.dto.response.*;

@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface ProfilePresentationMapper {

  @Mapping(target = "nickname", source = "userResult.nickname")
  @Mapping(target = "introduction", source = "profile.introduction")
  @Mapping(target = "timeZone", source = "profile.timeZone")
  @Mapping(target = "locale", source = "profile.locale")
  @Mapping(target = "theme", source = "profile.theme")
  @Mapping(target = "imageUrl", source = "profile.imageUrl")
  ProfileResponse toProfileResponse(UserResult userResult, Profile profile);

  @Mapping(target = "imageUrl", source = "imageUrl")
  ProfileImageUploadResponse toProfileImageUploadResponse(String imageUrl);

  @Mapping(target = "completedTodos", source = "succeededTodosCount")
  @Mapping(target = "totalCategories", source = "categoryCount")
  StatisticsResponse toStatisticsResponse(Statistics statistics);
}
