package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import point.ttodoApi.user.domain.User;

import java.time.LocalDateTime;
import java.util.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChallengeParticipation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotNull(message = USER_REQUIRED_MESSAGE)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotNull(message = CHALLENGE_REQUIRED_MESSAGE)
  private Challenge challenge;

  private LocalDateTime joinedAt;

  private LocalDateTime joinOut;

  @OneToMany(mappedBy = "challengeParticipation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChallengeTodo> challengeTodos = new ArrayList<>();

  @Builder
  public ChallengeParticipation(User user, Challenge challenge, LocalDateTime joinedAt) {
    this.user = user;
    this.challenge = challenge;
    this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
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

  // 테스트용 setter
  public void setJoinedAt(LocalDateTime joinedAt) {
    this.joinedAt = joinedAt;
  }
}
