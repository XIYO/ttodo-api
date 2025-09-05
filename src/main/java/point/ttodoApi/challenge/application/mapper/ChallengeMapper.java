package point.ttodoApi.challenge.application.mapper;

import org.mapstruct.*;
import point.ttodoApi.challenge.application.dto.result.*;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.shared.config.MapStructConfig;

@Mapper(config = MapStructConfig.class)
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
    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "email", source = "member.email")
    @Mapping(target = "nickname", source = "member.nickname")
    @Mapping(target = "joinedAt", source = "joinedAt")
    ParticipantResult toParticipantResult(ChallengeParticipation participation);
}