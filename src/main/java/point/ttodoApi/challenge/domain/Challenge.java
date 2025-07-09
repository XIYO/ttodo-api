package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType periodType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeVisibility visibility = ChallengeVisibility.PUBLIC;
    
    @Column(unique = true)
    private String inviteCode;
    
    private Integer maxParticipants;
    
    @Column(nullable = false)
    private UUID creatorId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false) 
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeParticipation> participations = new ArrayList<>();

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
        challenge.createdAt = LocalDateTime.now();
        challenge.updatedAt = LocalDateTime.now();
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

    public void update(String title, String description, PeriodType periodType, Integer maxParticipants) {
        this.title = title;
        this.description = description;
        this.periodType = periodType;
        this.maxParticipants = maxParticipants;
        this.updatedAt = LocalDateTime.now();
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
}
