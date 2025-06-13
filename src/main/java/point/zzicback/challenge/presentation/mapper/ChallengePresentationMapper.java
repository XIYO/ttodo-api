package point.zzicback.challenge.presentation.mapper;

import org.mapstruct.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChallengePresentationMapper {

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
                        .count()
        );
    }

    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "email", source = "member.email")
    @Mapping(target = "nickname", source = "member.nickname")
    @Mapping(target = "joinedAt", source = "joinedAt")
    ParticipantDto toParticipantDto(ChallengeParticipation participation);

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
