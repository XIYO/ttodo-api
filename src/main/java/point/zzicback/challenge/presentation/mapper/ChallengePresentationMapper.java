package point.zzicback.challenge.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.result.ChallengeListResult;
import point.zzicback.challenge.application.dto.result.ChallengeResult;
import point.zzicback.challenge.application.dto.result.ParticipantResult;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.presentation.dto.ChallengeResponse;
import point.zzicback.challenge.presentation.dto.ChallengeDetailResponse;
import point.zzicback.challenge.presentation.dto.CreateChallengeRequest;
import point.zzicback.challenge.presentation.dto.ParticipantResponse;
import point.zzicback.challenge.presentation.dto.UpdateChallengeRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChallengePresentationMapper {

    /** Presentation 레이어 요청 DTO -> Application Command 변환 */
    CreateChallengeCommand toCommand(CreateChallengeRequest request);

    /** Presentation 레이어 요청 DTO -> Application Command 변환 */
    UpdateChallengeCommand toCommand(UpdateChallengeRequest request);

    /** Application 결과 DTO -> Presentation 응답 변환 */
    ChallengeResponse toResponse(ChallengeListResult dto);

    /** Application 상세 결과 DTO -> Presentation 응답 변환 */
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
                null
        );
    }

    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "email", source = "member.email")
    @Mapping(target = "nickname", source = "member.nickname")
    @Mapping(target = "joinedAt", source = "joinedAt")
    ParticipantResult toParticipantResult(ChallengeParticipation participation);

    /** Application DTO -> Presentation 레이어 응답 DTO 변환 */
    ParticipantResponse toResponse(ParticipantResult dto);

    default ChallengeDetailResult toDetailResult(Challenge challenge) {
        if (challenge == null) return null;
        
        List<ParticipantResult> activeParticipants = challenge.getParticipations().stream()
                .filter(participation -> participation.getJoinOut() == null)
                .map(this::toParticipantResult)
                .toList();
        
        return new ChallengeDetailResult(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                activeParticipants
        );
    }
}
