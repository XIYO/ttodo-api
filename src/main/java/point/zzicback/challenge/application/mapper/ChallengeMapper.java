package point.zzicback.challenge.application.mapper;

import org.mapstruct.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.*;
import point.zzicback.common.config.MapStructConfig;

@Mapper(config = MapStructConfig.class)
public interface ChallengeMapper {
    
    // 기본 Challenge 매핑 (공통 필드)
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "challenge.id")
    @Mapping(target = "title", source = "challenge.title")
    @Mapping(target = "description", source = "challenge.description")
    @Mapping(target = "periodType", source = "challenge.periodType")
    @Mapping(target = "startDate", source = "challenge.startDate")
    @Mapping(target = "endDate", source = "challenge.endDate")
    ChallengeListResult toBaseResult(Challenge challenge);
    
    // 목록용 Result
    @InheritConfiguration(name = "toBaseResult")
    @Mapping(target = "participationStatus", source = "participationStatus")
    @Mapping(target = "activeParticipantCount", source = "activeParticipantCount")
    ChallengeListResult toListResult(Challenge challenge, boolean participationStatus, int activeParticipantCount);

    // 상세용 Result (추가 필드 포함)
    @InheritConfiguration(name = "toListResult")
    @Mapping(target = "successRate", source = "successRate")
    @Mapping(target = "completedCount", source = "completedCount")
    @Mapping(target = "totalCount", source = "totalCount")
    ChallengeResult toResult(Challenge challenge, boolean participationStatus, int activeParticipantCount, float successRate, int completedCount, int totalCount);
}