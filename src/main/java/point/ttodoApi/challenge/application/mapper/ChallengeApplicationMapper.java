package point.ttodoApi.challenge.application.mapper;

import org.mapstruct.Mapper;
import point.ttodoApi.shared.config.MapStructConfig;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import point.ttodoApi.challenge.application.command.CreateChallengeCommand;
import point.ttodoApi.challenge.application.result.ChallengeResult;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.challenge.domain.ChallengeVisibility;

/**
 * Challenge Application Mapper
 * TTODO 아키텍처 패턴: Application Layer 매퍼
 * Domain ↔ Application DTO 변환
 */
@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface ChallengeApplicationMapper {

    /**
     * CreateChallengeCommand에서 Challenge 생성 정보 추출
     * 실제 엔티티 생성은 서비스에서 팩토리 메서드 사용
     */
    default Challenge createChallenge(CreateChallengeCommand command) {
        return Challenge.builder()
                .title(command.title())
                .description(command.description())
                .periodType(command.periodType())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .creatorId(command.creatorId())
                .maxParticipants(command.maxParticipants())
                .visibility(command.visibility())
                .inviteCode(command.visibility() == ChallengeVisibility.INVITE_ONLY ? 
                        java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase() : null)
                .build();
    }

    /**
     * Challenge 엔티티 → ChallengeResult 변환
     */
    @Mapping(target = "participationStatus", ignore = true) // 서비스에서 설정
    @Mapping(target = "activeParticipantCount", expression = "java((int) challenge.getActiveParticipantCount())")
    @Mapping(target = "successRate", ignore = true) // 서비스에서 계산
    ChallengeResult toResult(Challenge challenge);
    
    /**
     * Challenge 엔티티 → ChallengeResult 변환 (참여 상태 포함)
     */
    default ChallengeResult toChallengeResult(Challenge challenge, Boolean participationStatus, Double successRate) {
        ChallengeResult result = toResult(challenge);
        return new ChallengeResult(
            result.id(),
            result.title(),
            result.description(),
            result.startDate(),
            result.endDate(),
            result.periodType(),
            participationStatus,
            result.activeParticipantCount(),
            successRate,
            result.visibility(),
            result.creatorId()
        );
    }
}
