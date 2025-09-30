package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.challenge.domain.validation.*;
import point.ttodoApi.shared.domain.BaseEntity;

import java.time.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

@Entity
@Getter
@Setter // 일반 Setter 제공
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수
@AllArgsConstructor(access = AccessLevel.PACKAGE) // MapStruct/테스트용
@Builder
@ToString(exclude = {"challengeParticipation"}) // 순환참조 방지
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE) // private 자동 적용
public class ChallengeTodo extends BaseEntity {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;
  
  @ManyToOne
  @NotNull(message = CHALLENGE_PARTICIPATION_REQUIRED_MESSAGE)
  ChallengeParticipation challengeParticipation;
  
  @Column(nullable = false)
  @NotNull(message = DONE_REQUIRED_MESSAGE)
  @Builder.Default
  Boolean done = false;
  
  @Column(nullable = false)
  @NotNull(message = TARGET_DATE_REQUIRED_MESSAGE)
  LocalDate targetDate;
  
  @Embedded
  @ValidPeriod
  Period period;



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
