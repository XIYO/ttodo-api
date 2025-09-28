package point.ttodoApi.challenge.application.mapper;

import org.mapstruct.*;
import point.ttodoApi.challenge.application.result.*;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.shared.config.shared.MapStructConfig;

@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface ChallengeMapper {

  // 목록용 Result
  @Mapping(target = "participationStatus", constant = "false")
  @Mapping(target = "activeParticipantCount", expression = "java((int)challenge.getActiveParticipantCount())")
  ChallengeListResult toListResult(Challenge challenge);

  // 상세용 Result
  @Mapping(target = "participationStatus", constant = "false")
  @Mapping(target = "activeParticipantCount", expression = "java((int)challenge.getActiveParticipantCount())")
  @Mapping(target = "visibility", source = "visibility")
  @Mapping(target = "creatorId", source = "creatorId")
  @Mapping(target = "successRate", constant = "0.0")
  ChallengeResult toDetailResult(Challenge challenge);

  // 참여자 Result
  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "nickname", ignore = true) // Profile에서 별도로 가져와야 함
  @Mapping(target = "joinedAt", source = "joinedAt")
  ParticipantResult toParticipantResult(ChallengeParticipation participation);
}