package point.zzicback.challenge.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.challenge.application.dto.result.ChallengeJoinedResult;
import point.zzicback.challenge.application.dto.result.ChallengeListResult;
import point.zzicback.challenge.application.dto.result.ChallengeResult;
import point.zzicback.challenge.application.dto.result.ChallengeDetailResult;
import point.zzicback.challenge.application.dto.result.ParticipantResult;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;

import java.util.List;

/**
 * MapStruct mapper for converting Challenge domain entities to application layer DTOs.
 */
@Mapper(componentModel = "spring")
public interface ChallengeMapper {
    /**
     * Convert Challenge to ChallengeListResult including participation status and active participant count.
     * @param challenge domain entity
     * @param participationStatus whether the current member has participated
     * @param activeParticipantCount count of active participants
     * @return DTO for list view
     */
    @Mapping(target = "participationStatus", source = "participationStatus")
    @Mapping(target = "activeParticipantCount", source = "activeParticipantCount")
    ChallengeListResult toListResult(Challenge challenge, boolean participationStatus, int activeParticipantCount);

    /**
     * Convert Challenge to ChallengeJoinedResult for member-specific listing.
     * @param challenge domain entity
     * @param participationStatus whether the member has joined
     * @return joined status DTO
     */
    @Mapping(target = "participationStatus", source = "participationStatus")
    ChallengeJoinedResult toJoinedResult(Challenge challenge, boolean participationStatus);

    /**
     * Convert Challenge to ChallengeResult including participation status, active count, and success rate.
     * @param challenge domain entity
     * @param participationStatus whether the member has participated
     * @param activeParticipantCount count of active participants
     * @param successRate completion rate
     * @param completedCount count of participants who completed challenge todos
     * @param totalCount total count of participants (including those who left)
     * @return detailed result DTO
     */
    @Mapping(target = "participationStatus", source = "participationStatus")
    @Mapping(target = "activeParticipantCount", source = "activeParticipantCount")
    @Mapping(target = "successRate", source = "successRate")
    @Mapping(target = "completedCount", source = "completedCount")
    @Mapping(target = "totalCount", source = "totalCount")
    ChallengeResult toResult(Challenge challenge, boolean participationStatus, int activeParticipantCount, float successRate, int completedCount, int totalCount);

    /**
     * Convert Challenge to ChallengeDetailResult including participant details.
     * @param challenge domain entity
     * @param participationStatus whether the member has participated
     * @param activeParticipantCount count of active participants
     * @param participants list of participant results
     * @return detailed DTO with participant list
     */
    @Mapping(target = "participationStatus", source = "participationStatus")
    @Mapping(target = "activeParticipantCount", source = "activeParticipantCount")
    @Mapping(target = "participants", source = "participants")
    ChallengeDetailResult toDetailResult(Challenge challenge, boolean participationStatus, int activeParticipantCount, List<ParticipantResult> participants);

    /**
     * Convert ChallengeParticipation to ParticipantResult.
     * @param participation domain participation
     * @return participant DTO
     */
    @Mapping(target = "id", source = "participation.member.id")
    @Mapping(target = "email", source = "participation.member.email")
    @Mapping(target = "nickname", source = "participation.member.nickname")
    @Mapping(target = "joinedAt", source = "participation.joinedAt")
    ParticipantResult toParticipantResult(ChallengeParticipation participation);
}