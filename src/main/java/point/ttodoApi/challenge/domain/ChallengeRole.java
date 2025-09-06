package point.ttodoApi.challenge.domain;

import lombok.Getter;

/**
 * 챌린지 내 멤버 역할
 */
@Getter
public enum ChallengeRole {
  /**
   * 챌린지 생성자 (전체 권한)
   */
  OWNER("생성자", "챌린지 생성자로 모든 권한을 가짐"),

  /**
   * 챌린지 리더 (참여자 관리 권한)
   */
  LEADER("리더", "참여자 관리 권한을 가진 리더"),

  /**
   * 일반 참여자 (기본 참여 권한)
   */
  PARTICIPANT("참여자", "챌린지에 참여하는 일반 멤버"),

  /**
   * 권한 없음 (비참여자)
   */
  NONE("비참여자", "챌린지에 참여하지 않은 멤버");

  private final String displayName;
  private final String description;

  ChallengeRole(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  /**
   * 참여자 관리 권한이 있는지 확인
   */
  public boolean canManageParticipants() {
    return this == OWNER || this == LEADER;
  }

  /**
   * 리더 관리 권한이 있는지 확인 (owner만)
   */
  public boolean canManageLeaders() {
    return this == OWNER;
  }

  /**
   * 챌린지 설정 변경 권한이 있는지 확인 (owner만)
   */
  public boolean canModifyChallenge() {
    return this == OWNER;
  }

  /**
   * 챌린지 참여 권한이 있는지 확인
   */
  public boolean canParticipate() {
    return this != NONE;
  }
}