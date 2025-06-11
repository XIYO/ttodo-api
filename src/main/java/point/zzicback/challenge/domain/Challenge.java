package point.zzicback.challenge.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeParticipation> participations = new ArrayList<>();

    @Builder
    public Challenge(String title, String description, PeriodType periodType, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.periodType = periodType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(String title, String description, PeriodType periodType) {
        this.title = title;
        this.description = description;
        this.periodType = periodType;
    }
}
