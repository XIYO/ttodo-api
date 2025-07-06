package point.zzicback.challenge.presentation.mapper;

import org.mapstruct.*;
import point.zzicback.challenge.application.dto.command.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.presentation.dto.request.*;
import point.zzicback.challenge.presentation.dto.response.*;
import point.zzicback.common.config.MapStructConfig;

@Mapper(config = MapStructConfig.class)
public interface ChallengePresentationMapper {

    /** Presentation 레이어 요청 DTO -> Application Command 변환 */
    CreateChallengeCommand toCommand(CreateChallengeRequest request);

    /** Presentation 레이어 요청 DTO -> Application Command 변환 */
    UpdateChallengeCommand toCommand(UpdateChallengeRequest request);

    /** Application 결과 DTO -> Presentation 응답 변환 */
    @Mapping(target = "participated", source = "participationStatus")
    @Mapping(target = "participantCount", source = "activeParticipantCount")
    ChallengeResponse toResponse(ChallengeListResult dto);

    /** Application 상세 결과 DTO -> Presentation 응답 변환 */
    @InheritConfiguration(name = "toResponse")
    @Mapping(target = "participants", ignore = true)
    ChallengeDetailResponse toResponse(ChallengeResult dto);

    default ChallengeResult toResult(Challenge challenge) {
        if (challenge == null) return null;
        return new ChallengeResult(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                false,
                (int) challenge.getParticipations().stream()
                        .filter(participation -> participation.getJoinOut() == null)
                        .count(),
                null,
                null,
                null
        );
    }

    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "email", source = "member.email")
    @Mapping(target = "nickname", source = "member.nickname")
    ParticipantResult toParticipantResult(ChallengeParticipation participation);

    /** Application DTO -> Presentation 레이어 응답 DTO 변환 */
    ParticipantResponse toResponse(ParticipantResult dto);
    
}
