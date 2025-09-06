package point.ttodoApi.challenge.domain;

import lombok.*;

/**
 * 챌린지 상태를 나타내는 열거형
 */
@Getter
@RequiredArgsConstructor
public enum ChallengeStatus {
  UPCOMING("예정됨", "챌린지가 아직 시작되지 않았습니다"),
  ACTIVE("진행중", "챌린지가 현재 진행 중입니다"),
  COMPLETED("완료됨", "챌린지가 종료되었습니다"),
  CANCELLED("취소됨", "챌린지가 취소되었습니다");

  private final String displayName;
  private final String description;

  /**
   * 챌린지가 참여 가능한 상태인지 확인
   *
   * @return 참여 가능 여부
   */
  public boolean isJoinable() {
    return this == UPCOMING || this == ACTIVE;
  }

  /**
   * 챌린지가 수정 가능한 상태인지 확인
   *
   * @return 수정 가능 여부
   */
  public boolean isEditable() {
    return this == UPCOMING;
  }
}