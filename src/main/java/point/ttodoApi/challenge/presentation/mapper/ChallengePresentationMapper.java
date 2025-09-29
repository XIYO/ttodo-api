package point.ttodoApi.challenge.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.challenge.application.command.*;
import point.ttodoApi.challenge.application.result.*;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.presentation.dto.request.*;
import point.ttodoApi.challenge.presentation.dto.response.*;
import point.ttodoApi.shared.config.MapStructConfig;

import java.util.UUID;

@Mapper(config = MapStructConfig.class, imports = {UUID.class})
@SuppressWarnings("NullableProblems")
public interface ChallengePresentationMapper {

  /**
   * Presentation 레이어 요청 DTO -> Application Command 변환
   */
  @Mapping(target = "creatorId", source = "creatorId")
  CreateChallengeCommand toCommand(CreateChallengeRequest request, UUID creatorId);

  /**
   * Presentation 레이어 요청 DTO -> Application Command 변환
   */
  UpdateChallengeCommand toCommand(UpdateChallengeRequest request);

  /**
   * Application 결과 DTO -> Presentation 응답 변환
   */
  @Mapping(target = "participated", source = "participationStatus")
  @Mapping(target = "participantCount", source = "activeParticipantCount")
  ChallengeResponse toResponse(ChallengeListResult dto);

  /**
   * Application 상세 결과 DTO -> Presentation 간단 응답 변환
   */
  @Mapping(target = "participated", source = "participationStatus")
  @Mapping(target = "participantCount", source = "activeParticipantCount")
  ChallengeResponse toChallengeResponse(ChallengeResult dto);

  /**
   * Application 상세 결과 DTO -> Presentation 응답 변환
   */
  @Mapping(target = "participated", source = "participationStatus")
  @Mapping(target = "participantCount", source = "activeParticipantCount")
  @Mapping(target = "successRate", expression = "java(dto.successRate() != null ? dto.successRate().floatValue() : 0.0f)")
  @Mapping(target = "completedCount", constant = "0")
  @Mapping(target = "totalCount", source = "activeParticipantCount")
  @Mapping(target = "participants", ignore = true)
  ChallengeDetailResponse toResponse(ChallengeResult dto);


  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "nickname", ignore = true) // Profile에서 별도로 가져와야 함
  @Mapping(target = "joinedAt", source = "joinedAt")
  ParticipantResult toParticipantResult(ChallengeParticipation participation);

  /**
   * Application DTO -> Presentation 레이어 응답 DTO 변환
   */
  ParticipantResponse toResponse(ParticipantResult dto);

  /**
   * 가시성 Enum -> Response 변환
   */
  default ChallengeVisibilityResponse toResponse(ChallengeVisibility visibility) {
    return new ChallengeVisibilityResponse(
            visibility.name(),
            visibility.getDescription()
    );
  }

  /**
   * Domain Entity -> Presentation 응답 변환
   */
  @Mapping(target = "participated", constant = "false")
  @Mapping(target = "participantCount", expression = "java(challenge.getParticipations() != null ? challenge.getParticipations().size() : 0)")
  ChallengeResponse toChallengeSummaryResponse(Challenge challenge);

}