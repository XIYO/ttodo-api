package point.ttodoApi.challenge.presentation.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.challenge.domain.ChallengeLeader;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 챌린지 리더 응답 DTO
 * 롬복 최적화: @FieldDefaults + @Builder 패턴
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString // 디버깅을 위한 toString 추가
@FieldDefaults(level = AccessLevel.PRIVATE) // private 자동 적용
public class ChallengeLeaderResponse {

  Long id;
  Long challengeId;
  String challengeTitle;
  UUID userId;
  String userNickname;
  String userEmail;
  LocalDateTime appointedAt;
  UUID appointedBy;
  String appointedByNickname;
  String status;
  LocalDateTime removedAt;
  UUID removedBy;
  String removedByNickname;
  String removalReason;

  public static ChallengeLeaderResponse from(ChallengeLeader leader, String userNickname) {
    return ChallengeLeaderResponse.builder()
            .id(leader.getId())
            .challengeId(leader.getChallenge().getId())
            .challengeTitle(leader.getChallenge().getTitle())
            .userId(leader.getUser().getId())
            .userNickname(userNickname)  // Profile에서 가져온 nickname을 매개변수로 받음
            .userEmail(leader.getUser().getEmail())
            .appointedAt(leader.getAppointedAt())
            .appointedBy(leader.getAppointedBy())
            .status(leader.getStatus().name())
            .removedAt(leader.getRemovedAt())
            .removedBy(leader.getRemovedBy())
            .removalReason(leader.getRemovalReason())
            .build();
  }

  public static ChallengeLeaderResponse fromWithAppointerInfo(ChallengeLeader leader,
                                                              String userNickname,
                                                              String appointedByNickname,
                                                              String removedByNickname) {
    ChallengeLeaderResponse response = from(leader, userNickname);
    response.appointedByNickname = appointedByNickname;
    response.removedByNickname = removedByNickname;
    return response;
  }
}