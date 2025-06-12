package point.zzicback.challenge.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

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
                LocalDate weekEnd = targetDate.plusWeeks(1);
                return !date.isBefore(targetDate) && date.isBefore(weekEnd);
            case MONTHLY:
                LocalDate monthEnd = targetDate.plusMonths(1);
                return !date.isBefore(targetDate) && date.isBefore(monthEnd);
            default:
                return false;
        }
    }
}
