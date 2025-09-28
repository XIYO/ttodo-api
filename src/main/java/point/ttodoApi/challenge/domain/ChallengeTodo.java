package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import point.ttodoApi.challenge.domain.validation.*;

import java.time.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChallengeTodo {

  @ManyToOne
  @NotNull(message = CHALLENGE_PARTICIPATION_REQUIRED_MESSAGE)
  ChallengeParticipation challengeParticipation;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  @NotNull(message = DONE_REQUIRED_MESSAGE)
  private Boolean done = false;
  @Column(nullable = false)
  @NotNull(message = TARGET_DATE_REQUIRED_MESSAGE)
  private LocalDate targetDate;
  @Embedded
  @ValidPeriod
  private Period period;

  @Builder
  public ChallengeTodo(ChallengeParticipation challengeParticipation, Period period, LocalDate targetDate) {
    this.challengeParticipation = challengeParticipation;
    this.period = period;
    this.targetDate = targetDate;
    this.done = false;
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void complete(LocalDate currentDate) {
    if (!isInPeriod(challengeParticipation.getChallenge().getPeriodType(), currentDate)) {
      throw new IllegalStateException("챌린지 기간이 아닐 때는 완료할 수 없습니다.");
    }
    if (this.done) {
      throw new IllegalStateException("이미 완료된 챌린지입니다.");
    }
    this.done = true;
  }

  public boolean isCompleted() {
    return this.done;
  }

  public PeriodType.PeriodRange getPeriod() {
    return challengeParticipation.getChallenge().getPeriodType().calculatePeriod(LocalDate.now());
  }

  public boolean isInPeriod(PeriodType periodType, LocalDate date) {
    switch (periodType) {
      case DAILY:
        return targetDate.equals(date);
      case WEEKLY:
        LocalDate weekEnd = targetDate.plusWeeks(1);
        return !date.isBefore(targetDate) && date.isBefore(weekEnd);
      case MONTHLY:
        LocalDate monthEnd = targetDate.plusMonths(1);
        return !date.isBefore(targetDate) && date.isBefore(monthEnd);
      default:
        return false;
    }
  }

  public void cancel() {
    this.done = false;
  }
}
