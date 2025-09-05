package point.ttodoApi.challenge.presentation.dto.response;

import lombok.*;
import point.ttodoApi.challenge.domain.ChallengeLeader;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 챌린지 리더 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeLeaderResponse {
    
    private Long id;
    private Long challengeId;
    private String challengeTitle;
    private UUID memberId;
    private String memberNickname;
    private String memberEmail;
    private LocalDateTime appointedAt;
    private UUID appointedBy;
    private String appointedByNickname;
    private String status;
    private LocalDateTime removedAt;
    private UUID removedBy;
    private String removedByNickname;
    private String removalReason;
    
    public static ChallengeLeaderResponse from(ChallengeLeader leader) {
        return ChallengeLeaderResponse.builder()
            .id(leader.getId())
            .challengeId(leader.getChallenge().getId())
            .challengeTitle(leader.getChallenge().getTitle())
            .memberId(leader.getMember().getId())
            .memberNickname(leader.getMember().getNickname())
            .memberEmail(leader.getMember().getEmail())
            .appointedAt(leader.getAppointedAt())
            .appointedBy(leader.getAppointedBy())
            .status(leader.getStatus().name())
            .removedAt(leader.getRemovedAt())
            .removedBy(leader.getRemovedBy())
            .removalReason(leader.getRemovalReason())
            .build();
    }
    
    public static ChallengeLeaderResponse fromWithAppointerInfo(ChallengeLeader leader, 
                                                               String appointedByNickname,
                                                               String removedByNickname) {
        ChallengeLeaderResponse response = from(leader);
        response.appointedByNickname = appointedByNickname;
        response.removedByNickname = removedByNickname;
        return response;
    }
}