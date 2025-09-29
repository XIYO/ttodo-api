package point.ttodoApi.user.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.user.application.command.UpdateUserCommand;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.presentation.dto.request.UpdateUserRequest;
import point.ttodoApi.user.presentation.dto.response.*;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.shared.config.MapStructConfig;

import java.util.UUID;

/**
 * Presentation 레이어 요청/응답과 Application/Domain DTO 간 변환을 담당하는 Mapper
 */
@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface UserPresentationMapper {

  /**
   * Presentation 요청 DTO -> Application Command 변환
   */
  UpdateUserCommand toCommand(UUID userId, UpdateUserRequest request);

  /**
   * Domain User -> Application UserResult 변환
   */
  @Mapping(target = "nickname", ignore = true)
  UserResult toResult(User user);

  /**
   * Application UserResult -> Presentation Response DTO 변환
   */
  @Mapping(source = "dto.id", target = "id")
  @Mapping(source = "dto.email", target = "email")
  @Mapping(source = "dto.nickname", target = "nickname")
  @Mapping(source = "profile.introduction", target = "introduction")
  @Mapping(source = "profile.locale", target = "locale")
  @Mapping(source = "profile.timeZone", target = "timeZone")
  @Mapping(source = "profile.theme", target = "theme")
  @Mapping(target = "profileImageUrl", expression = "java(profile != null ? profile.getImageUrl() : null)")
  UserResponse toResponse(UserResult dto, Profile profile);

  /**
   * Simple version without profile (for list views)
   */
  @Mapping(target = "introduction", constant = "")
  @Mapping(target = "locale", constant = "ko-KR")
  @Mapping(target = "timeZone", constant = "Asia/Seoul")
  @Mapping(target = "theme", constant = "LIGHT")
  @Mapping(target = "profileImageUrl", expression = "java(null)")
  UserResponse toResponse(UserResult dto);

  /**
   * Domain Entity to Response mapping
   */
  @Mapping(target = "nickname", ignore = true)
  @Mapping(target = "introduction", constant = "")
  @Mapping(target = "locale", constant = "ko-KR")
  @Mapping(target = "timeZone", constant = "Asia/Seoul")
  @Mapping(target = "theme", constant = "LIGHT")
  @Mapping(target = "profileImageUrl", expression = "java(null)")
  UserResponse toResponse(User user);
}
