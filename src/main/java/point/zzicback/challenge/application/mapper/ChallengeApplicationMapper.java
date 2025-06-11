package point.zzicback.challenge.application.mapper;

import org.mapstruct.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChallengeApplicationMapper {

    ChallengeDto toChallengeDto(Challenge challenge);

    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "email", source = "member.email")
    @Mapping(target = "nickname", source = "member.nickname")
    @Mapping(target = "joinedAt", source = "joinedAt")
    ParticipantDto toParticipantDto(ChallengeParticipation participation);

    @Mapping(target = "participants", source = "participations")
    ChallengeDetailDto toChallengeDetailDto(Challenge challenge);

    List<ChallengeDto> toChallengeDto(List<Challenge> challenges);

    List<ChallengeDetailDto> toChallengeDetailDto(List<Challenge> challenges);
}