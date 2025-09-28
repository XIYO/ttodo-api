package point.ttodoApi.profile.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import point.ttodoApi.profile.domain.validation.*;
import point.ttodoApi.user.domain.User;

import java.time.LocalDateTime;

import static point.ttodoApi.profile.domain.StatisticsConstants.*;

@Entity
@Table(name = "statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistics {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false, unique = true)
  @NotNull(message = OWNER_REQUIRED_MESSAGE)
  private User owner;

  @Column
  @ValidSucceededTodosCount
  private Integer succeededTodosCount;

  @Column
  @ValidSucceededChallengesCount
  private Integer succeededChallengesCount;

  @Column
  @ValidFocusTime
  private Long totalFocusTime; // in seconds

  @Column
  @ValidCurrentStreakDays
  private Integer currentStreakDays;

  @Column
  @ValidLongestStreakDays
  private Integer longestStreakDays;

  @Column
  @ValidCategoryCount
  private Integer categoryCount;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public Statistics(User owner, @ValidSucceededTodosCount Integer succeededTodosCount, @ValidCategoryCount Integer categoryCount) {
    this.owner = owner;
    this.succeededTodosCount = succeededTodosCount != null ? succeededTodosCount : DEFAULT_COUNT;
    this.categoryCount = categoryCount != null ? categoryCount : DEFAULT_COUNT;
    this.succeededChallengesCount = DEFAULT_COUNT;
    this.totalFocusTime = DEFAULT_FOCUS_TIME;
    this.currentStreakDays = DEFAULT_STREAK_DAYS;
    this.longestStreakDays = DEFAULT_STREAK_DAYS;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 통계 업데이트 (완료한 할일 수, 카테고리 수)
   */
  public void updateStatistics(@ValidSucceededTodosCount Integer succeededTodosCount, @ValidCategoryCount Integer categoryCount) {
    this.succeededTodosCount = succeededTodosCount != null ? succeededTodosCount : DEFAULT_COUNT;
    this.categoryCount = categoryCount != null ? categoryCount : DEFAULT_COUNT;
    this.updatedAt = LocalDateTime.now();
  }

  @PrePersist
  private void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  private void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
