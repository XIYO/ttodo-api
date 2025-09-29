package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.challenge.domain.validation.*;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.domain.BaseEntity;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

@Entity
@Getter
@Setter // 일반 Setter 제공
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수
@AllArgsConstructor(access = AccessLevel.PACKAGE) // MapStruct/테스트용
@Builder
@ToString(exclude = {"participations", "leaders"}) // 순환참조 방지
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE) // private 자동 적용
@ValidChallengePeriod
public class Challenge extends BaseEntity {
  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, length = TITLE_MAX_LENGTH)
  @ValidChallengeTitle
  String title;

  @Column(length = DESCRIPTION_MAX_LENGTH)
  @ValidChallengeDescription
  String description;

  @Column(nullable = false)
  @NotNull(message = START_DATE_REQUIRED_MESSAGE)
  @ValidChallengeDate
  LocalDate startDate;

  @Column(nullable = false)
  @NotNull(message = END_DATE_REQUIRED_MESSAGE)
  @ValidChallengeDate
  LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @NotNull(message = PERIOD_TYPE_REQUIRED_MESSAGE)
  PeriodType periodType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @NotNull(message = VISIBILITY_REQUIRED_MESSAGE)
  @Builder.Default
  ChallengeVisibility visibility = DEFAULT_VISIBILITY;

  @Column(unique = true, length = INVITE_CODE_LENGTH)
  @ValidInviteCode
  String inviteCode;

  @ValidMaxParticipants
  Integer maxParticipants;

  @Column(nullable = false)
  @NotNull(message = CREATOR_ID_REQUIRED_MESSAGE)
  UUID creatorId;

  @Column(nullable = false)
  @NotNull(message = ACTIVE_REQUIRED_MESSAGE)
  @Builder.Default
  Boolean active = DEFAULT_ACTIVE;

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<ChallengeParticipation> participations = new ArrayList<>();

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<ChallengeLeader> leaders = new ArrayList<>();

  // 팩토리 메서드 - 공개 챌린지
  public static Challenge createPublicChallenge(String title, String description,
                                                PeriodType periodType, LocalDate startDate,
                                                LocalDate endDate, UUID creatorId,
                                                Integer maxParticipants) {
    Challenge challenge = new Challenge();
    challenge.title = title;
    challenge.description = description;
    challenge.periodType = periodType;
    challenge.startDate = startDate;
    challenge.endDate = endDate;
    challenge.creatorId = creatorId;
    challenge.maxParticipants = maxParticipants;
    challenge.visibility = ChallengeVisibility.PUBLIC;
    return challenge;
  }

  // 팩토리 메서드 - 초대 전용 챌린지
  public static Challenge createInviteOnlyChallenge(String title, String description,
                                                    PeriodType periodType, LocalDate startDate,
                                                    LocalDate endDate, UUID creatorId,
                                                    Integer maxParticipants) {
    Challenge challenge = createPublicChallenge(title, description, periodType,
            startDate, endDate, creatorId, maxParticipants);
    challenge.visibility = ChallengeVisibility.INVITE_ONLY;
    challenge.inviteCode = generateInviteCode();
    return challenge;
  }

  private static String generateInviteCode() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }


  // 비즈니스 메서드
  public boolean isJoinable() {
    if (maxParticipants != null) {
      long activeCount = participations.stream()
              .filter(p -> p.getJoinOut() == null)
              .count();
      return activeCount < maxParticipants;
    }
    return true;
  }

  public boolean isActive() {
    LocalDate now = LocalDate.now();
    return !now.isBefore(startDate) && !now.isAfter(endDate);
  }

  public long getActiveParticipantCount() {
    return participations.stream()
            .filter(p -> p.getJoinOut() == null)
            .count();
  }

  public int getCurrentParticipants() {
    return (int) getActiveParticipantCount();
  }

  // 리더 관리 메서드들

  /**
   * 챌린지 생성자인지 확인
   */
  public boolean isOwner(User user) {
    return user != null && this.creatorId.equals(user.getId());
  }

  /**
   * 챌린지 생성자인지 확인 (ID로)
   */
  public boolean isOwner(UUID userId) {
    return userId != null && this.creatorId.equals(userId);
  }

  /**
   * 활성 리더인지 확인
   */
  public boolean isLeader(User user) {
    if (user == null) return false;

    return leaders.stream()
            .anyMatch(leader -> leader.getUser().equals(user) && leader.isActive());
  }

  /**
   * 활성 리더인지 확인 (ID로)
   */
  public boolean isLeader(UUID userId) {
    if (userId == null) return false;

    return leaders.stream()
            .anyMatch(leader -> leader.getUser().getId().equals(userId) && leader.isActive());
  }

  /**
   * 참여자 관리 권한 확인 (owner 또는 leader)
   */
  public boolean canManageParticipants(User user) {
    return isOwner(user) || isLeader(user);
  }

  /**
   * 참여자 관리 권한 확인 (ID로)
   */
  public boolean canManageParticipants(UUID userId) {
    return isOwner(userId) || isLeader(userId);
  }

  /**
   * 리더 추가
   */
  public ChallengeLeader addLeader(User user, UUID appointedBy) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    // 이미 활성 리더인지 확인
    if (isLeader(user)) {
      throw new IllegalArgumentException("User is already a leader");
    }

    // 챌린지에 참여하지 않은 멤버는 리더가 될 수 없음
    if (!isParticipant(user)) {
      throw new IllegalArgumentException("User must be a participant to become a leader");
    }

    ChallengeLeader leader = new ChallengeLeader(this, user, appointedBy);
    this.leaders.add(leader);

    return leader;
  }

  /**
   * 리더 제거
   */
  public void removeLeader(User user, UUID removedBy, String reason) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    ChallengeLeader leader = leaders.stream()
            .filter(l -> l.getUser().equals(user) && l.isActive())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("User is not an active leader"));

    leader.removeLeader(removedBy, reason);
  }

  /**
   * 활성 리더 목록 조회
   */
  public List<ChallengeLeader> getActiveLeaders() {
    return leaders.stream()
            .filter(ChallengeLeader::isActive)
            .collect(Collectors.toList());
  }

  /**
   * 활성 리더 수 조회
   */
  public long getActiveLeaderCount() {
    return leaders.stream()
            .filter(ChallengeLeader::isActive)
            .count();
  }

  /**
   * 멤버가 챌린지 참여자인지 확인
   */
  public boolean isParticipant(User user) {
    if (user == null) return false;

    return participations.stream()
            .anyMatch(p -> p.getUser().getId().equals(user.getId()) && p.getJoinOut() == null);
  }

  /**
   * 멤버가 챌린지 참여자인지 확인 (ID로)
   */
  public boolean isParticipant(UUID userId) {
    if (userId == null) return false;

    return participations.stream()
            .anyMatch(p -> p.getUser().getId().equals(userId) && p.getJoinOut() == null);
  }

  /**
   * 챌린지에 대한 권한 확인 (owner, leader, participant 순서)
   */
  public ChallengeRole getUserRole(User user) {
    if (user == null) return ChallengeRole.NONE;

    if (isOwner(user)) return ChallengeRole.OWNER;
    if (isLeader(user)) return ChallengeRole.LEADER;
    if (isParticipant(user)) return ChallengeRole.PARTICIPANT;

    return ChallengeRole.NONE;
  }

  /**
   * 챌린지에 대한 권한 확인 (ID로)
   */
  public ChallengeRole getUserRole(UUID userId) {
    if (userId == null) return ChallengeRole.NONE;

    if (isOwner(userId)) return ChallengeRole.OWNER;
    if (isLeader(userId)) return ChallengeRole.LEADER;
    if (isParticipant(userId)) return ChallengeRole.PARTICIPANT;

    return ChallengeRole.NONE;
  }

  /**
   * 리더 최대 인원 제한 확인 (전체 참여자의 30% 또는 최소 1명)
   */
  public boolean canAddMoreLeaders() {
    long currentParticipants = getActiveParticipantCount();
    long currentLeaders = getActiveLeaderCount();

    // 참여자가 없으면 리더 추가 불가
    if (currentParticipants == 0) return false;

    // 최대 리더 수: 참여자의 30% (최소 1명, 최대 10명)
    long maxLeaders = Math.max(1, Math.min(10, currentParticipants * 30 / 100));

    return currentLeaders < maxLeaders;
  }

  /**
   * 소유권 확인 메서드 (Spring Security @PreAuthorize용)
   *
   * @param userId 확인할 멤버 ID
   * @return 소유자인지 여부
   */
  public boolean isOwn(UUID userId) {
    if (userId == null) return false;
    return this.creatorId.equals(userId);
  }
}
