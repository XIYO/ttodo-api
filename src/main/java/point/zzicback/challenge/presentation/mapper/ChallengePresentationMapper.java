package point.zzicback.challenge.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.result.ChallengeListDto;
import point.zzicback.challenge.application.dto.result.ChallengeDto;
import point.zzicback.challenge.application.dto.result.ParticipantDto;
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
    default CreateChallengeCommand toCommand(CreateChallengeRequest request) {
        if (request == null) return null;
        return new CreateChallengeCommand(
            request.title(),
            request.description(),
            request.periodType()
        );
    }

    default UpdateChallengeCommand toCommand(UpdateChallengeRequest request) {
        if (request == null) return null;
        return new UpdateChallengeCommand(
            request.title(),
            request.description(),
            request.periodType()
        );
    }

    /**
     * Domain Challenge -> Application ChallengeListDto -> Presentation ChallengeResponse
     */
    /** Presentation 응답 DTO (챌린지 요약) 생성 */
    default ChallengeResponse toResponse(ChallengeListDto dto) {
        if (dto == null) return null;
        return new ChallengeResponse(
            dto.id(), dto.title(), dto.description(),
            dto.startDate(), dto.endDate(), dto.periodType(),
            dto.participationStatus(), dto.activeParticipantCount()
        );
    }

    /**
     * Domain Challenge -> Application ChallengeDto -> Presentation ChallengeDetailResponse
     */
    /** Presentation 응답 DTO (챌린지 상세) 생성 */
    default ChallengeDetailResponse toResponse(ChallengeDto dto) {
        if (dto == null) return null;
        List<ParticipantResponse> resp = dto.participantDtos().stream()
            .map(this::toResponse)
            .toList();
        return new ChallengeDetailResponse(
            dto.id(), dto.title(), dto.description(),
            dto.startDate(), dto.endDate(), dto.periodType(),
            dto.participationStatus(), dto.activeParticipantCount(),
            dto.successRate(), resp
        );

    }

    default ChallengeDto toDto(Challenge challenge) {
        if (challenge == null) return null;
        return new ChallengeDto(
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
    ParticipantDto toParticipantDto(ChallengeParticipation participation);

    /** Application DTO -> Presentation 레이어 응답 DTO 변환 */
    ParticipantResponse toResponse(ParticipantDto dto);

    default ChallengeDetailDto toDetailDto(Challenge challenge) {
        if (challenge == null) return null;
        
        List<ParticipantDto> activeParticipants = challenge.getParticipations().stream()
                .filter(participation -> participation.getJoinOut() == null)
                .map(this::toParticipantDto)
                .toList();
        
        return new ChallengeDetailDto(
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
