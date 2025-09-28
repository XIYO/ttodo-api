package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import point.ttodoApi.challenge.domain.validation.*;
import point.ttodoApi.user.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

/**
 * 챌린지 리더(그룹장) 엔티티
 */
@Entity
@Table(name = "challenge_leaders",
        indexes = {
                @Index(name = "idx_challenge_leader_challenge", columnList = "challenge_id"),
                @Index(name = "idx_challenge_leader_user", columnList = "user_id"),
                @Index(name = "idx_challenge_leader_status", columnList = "challenge_id, status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_challenge_leader", columnNames = {"challenge_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeLeader {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "challenge_id", nullable = false)
  @NotNull(message = CHALLENGE_REQUIRED_MESSAGE)
  private Challenge challenge;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @NotNull(message = USER_REQUIRED_MESSAGE)
  private User user;

  @Column(name = "appointed_at", nullable = false)
  private LocalDateTime appointedAt;

  @Column(name = "appointed_by", nullable = false)
  @NotNull(message = APPOINTED_BY_REQUIRED_MESSAGE)
  private UUID appointedBy; // 리더를 지정한 사람 (일반적으로 챌린지 생성자)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @NotNull(message = STATUS_REQUIRED_MESSAGE)
  private LeaderStatus status = DEFAULT_LEADER_STATUS;

  @Column(name = "removed_at")
  private LocalDateTime removedAt;

  @Column(name = "removed_by")
  private UUID removedBy;

  @Column(name = "removal_reason", length = REMOVAL_REASON_MAX_LENGTH)
  @ValidRemovalReason
  private String removalReason;

  /**
   * 새 리더 임명 생성자
   */
  public ChallengeLeader(Challenge challenge, User user, UUID appointedBy) {
    this.challenge = challenge;
    this.user = user;
    this.appointedBy = appointedBy;
    this.appointedAt = LocalDateTime.now();
    this.status = LeaderStatus.ACTIVE;
  }

  /**
   * 리더 해제
   */
  public void removeLeader(UUID removedBy, String reason) {
    if (this.status == LeaderStatus.REMOVED) {
      throw new IllegalStateException("Leader is already removed");
    }

    this.status = LeaderStatus.REMOVED;
    this.removedAt = LocalDateTime.now();
    this.removedBy = removedBy;
    this.removalReason = reason;
  }

  /**
   * 활성 리더인지 확인
   */
  public boolean isActive() {
    return this.status == LeaderStatus.ACTIVE;
  }

  /**
   * 리더 복원 (제거된 리더를 다시 활성화)
   */
  public void restoreLeader(UUID restoredBy) {
    if (this.status != LeaderStatus.REMOVED) {
      throw new IllegalStateException("Only removed leader can be restored");
    }

    this.status = LeaderStatus.ACTIVE;
    this.removedAt = null;
    this.removedBy = null;
    this.removalReason = null;
    // appointedBy를 복원한 사람으로 업데이트
    this.appointedBy = restoredBy;
    this.appointedAt = LocalDateTime.now();
  }
}

