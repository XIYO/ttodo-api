package point.ttodoApi.experience.domain;

import jakarta.persistence.*;
import lombok.*;
import point.ttodoApi.experience.domain.validation.*;

import java.util.UUID;

import static point.ttodoApi.experience.domain.ExperienceConstants.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "user_experiences")
public class UserExperience {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @ValidOwnerId
  private UUID ownerId;

  @Column(nullable = false)
  @ValidExperience
  private int experience;

  public void addExperience(@ValidExperienceIncrement int amount) {
    experience += amount;
    // 최대값 초과 방지
    if (experience > MAX_EXPERIENCE) {
      experience = MAX_EXPERIENCE;
    }
  }

  public void subtractExperience(@ValidExperienceIncrement int amount) {
    experience = Math.max(MIN_EXPERIENCE, experience - amount);
  }
}
