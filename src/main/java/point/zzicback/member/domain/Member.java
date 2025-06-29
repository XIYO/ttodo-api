package point.zzicback.member.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  private UUID id;
  @Column(unique = true, nullable = false)
  private String email;
  @Column(nullable = false)
  private String nickname;
  @Column(nullable = false)
  private String password;
  @Column(length = 500)
  private String introduction;
  @Column(name = "time_zone", nullable = false)
  private String timeZone;
  @Column(nullable = false)
  private String locale;
}
