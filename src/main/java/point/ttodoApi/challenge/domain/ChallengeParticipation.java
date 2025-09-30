package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.domain.BaseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static point.ttodoApi.challenge.domain.ChallengeConstants.*;

@Entity
@Getter
@Setter // 일반 Setter 제공
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수
@AllArgsConstructor(access = AccessLevel.PACKAGE) // MapStruct/테스트용
@Builder
@ToString(exclude = {"user", "challenge", "challengeTodos"}) // 순환참조 방지
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE) // private 자동 적용
public class ChallengeParticipation extends BaseEntity {
  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotNull(message = USER_REQUIRED_MESSAGE)
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotNull(message = CHALLENGE_REQUIRED_MESSAGE)
  Challenge challenge;

  // 챌린지 참여 시작 시간 (시스템 createdAt와 논리적으로 구분)
  LocalDateTime joinedAt;
  
  LocalDateTime joinOut;

  @OneToMany(mappedBy = "challengeParticipation", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<ChallengeTodo> challengeTodos = new ArrayList<>();
  
  @PrePersist
  protected void onCreate() {
    if (joinedAt == null) {
      joinedAt = LocalDateTime.now();
    }
  }
}
