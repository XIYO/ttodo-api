package point.ttodoApi.experience.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_experiences")
public class MemberExperience {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private UUID ownerId;

  @Column(nullable = false)
  private int experience;

  @Builder
  public MemberExperience(UUID ownerId, int experience) {
    this.ownerId = ownerId;
    this.experience = experience;
  }

  public void addExperience(int amount) {
    experience += amount;
  }

  public void subtractExperience(int amount) {
    experience = Math.max(0, experience - amount);
  }
}
