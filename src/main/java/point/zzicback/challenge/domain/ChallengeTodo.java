package point.zzicback.challenge.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChallengeTodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    ChallengeParticipation challengeParticipation;

    @Column(nullable = false)
    private Boolean done = false;

    @Column(nullable = false)
    private LocalDate targetDate;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @Builder
    public ChallengeTodo(ChallengeParticipation challengeParticipation, LocalDate targetDate) {
        this.challengeParticipation = challengeParticipation;
        this.targetDate = targetDate;
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

    public void cancel(LocalDate currentDate) {
        if (!isInPeriod(challengeParticipation.getChallenge().getPeriodType(), currentDate)) {
            throw new IllegalStateException("챌린지 기간이 아닐 때는 취소할 수 없습니다.");
        }
        if (!this.done) {
            throw new IllegalStateException("완료되지 않은 챌린지는 취소할 수 없습니다.");
        }
        this.done = false;
    }

    public boolean isCompleted() {
        return this.done;
    }

    public PeriodType.PeriodRange getPeriod() {
        return challengeParticipation.getChallenge().getPeriodType().calculatePeriod(targetDate);
    }

    public boolean isInPeriod(PeriodType periodType, LocalDate date) {
        switch (periodType) {
            case DAILY:
                return targetDate.equals(date);
            case WEEKLY:
                LocalDate weekStart = targetDate.with(previousOrSame(MONDAY));
                LocalDate weekEnd = weekStart.with(nextOrSame(SUNDAY));
                return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
            case MONTHLY:
                return targetDate.getYear() == date.getYear() &&
                       targetDate.getMonth() == date.getMonth();
            default:
                return false;
        }
    }
}
