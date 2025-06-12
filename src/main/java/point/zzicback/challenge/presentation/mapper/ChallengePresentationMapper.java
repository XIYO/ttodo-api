package point.zzicback.challenge.presentation.mapper;

import org.mapstruct.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.*;
import point.zzicback.member.domain.Member;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChallengePresentationMapper {

    @Mapping(target = "participationStatus", constant = "false")
    ChallengeDto toDto(Challenge challenge);

    default ChallengeDto toDtoWithParticipation(Challenge challenge, boolean participationStatus) {
        return new ChallengeDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                participationStatus
        );
    }

    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "email", source = "member.email")
    @Mapping(target = "nickname", source = "member.nickname")
    @Mapping(target = "joinedAt", source = "joinedAt")
    ParticipantDto toParticipantDto(ChallengeParticipation participation);

    @Mapping(target = "participants", source = "participations")
    ChallengeDetailDto toDetailDto(Challenge challenge);

    default ChallengeJoinedDto toJoinedDto(Challenge challenge, boolean participationStatus) {
        return new ChallengeJoinedDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getPeriodType(),
                participationStatus
        );
    }

    List<ChallengeDto> toDto(List<Challenge> challenges);
    List<ChallengeDetailDto> toDetailDto(List<Challenge> challenges);
}
