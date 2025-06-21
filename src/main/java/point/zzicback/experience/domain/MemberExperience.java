package point.zzicback.experience.domain;

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
    private UUID memberId;

    @Column(nullable = false)
    private int experience;

    @Builder
    public MemberExperience(UUID memberId, int experience) {
        this.memberId = memberId;
        this.experience = experience;
    }

    public void addExperience(int amount) {
        this.experience += amount;
    }
}
