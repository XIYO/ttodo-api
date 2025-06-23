package point.zzicback.challenge.application.mapper;

import org.mapstruct.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.*;

@Mapper(componentModel = "spring")
public interface ChallengeMapper {
    
    @Mapping(target = "participationStatus", source = "participationStatus")
    @Mapping(target = "activeParticipantCount", source = "activeParticipantCount")
    ChallengeListResult toListResult(Challenge challenge, boolean participationStatus, int activeParticipantCount);

    @Mapping(target = "participationStatus", source = "participationStatus")
    @Mapping(target = "activeParticipantCount", source = "activeParticipantCount")
    @Mapping(target = "successRate", source = "successRate")
    @Mapping(target = "completedCount", source = "completedCount")
    @Mapping(target = "totalCount", source = "totalCount")
    ChallengeResult toResult(Challenge challenge, boolean participationStatus, int activeParticipantCount, float successRate, int completedCount, int totalCount);
}