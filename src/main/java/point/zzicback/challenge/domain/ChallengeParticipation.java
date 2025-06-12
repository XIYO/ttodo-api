package point.zzicback.challenge.domain;

import jakarta.persistence.*;
import lombok.*;
import point.zzicback.member.domain.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChallengeParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Challenge challenge;

    private LocalDateTime joinedAt;

    private LocalDateTime joinOut;

    @OneToMany(mappedBy = "challengeParticipation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeTodo> challengeTodos = new ArrayList<>();

    @Builder
    public ChallengeParticipation(Member member, Challenge challenge) {
        this.member = member;
        this.challenge = challenge;
    }

    @PrePersist
    private void prePersist() {
        joinedAt = LocalDateTime.now();
    }

    public void leaveChallenge() {
        this.joinOut = LocalDateTime.now();
    }

    public boolean isActive() {
        return joinOut == null;
    }

    public boolean hasLeftChallenge() {
        return joinOut != null;
    }
}
