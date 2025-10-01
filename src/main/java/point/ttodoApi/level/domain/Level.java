package point.ttodoApi.level.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "levels")
public class Level {
  @Id
  private Integer level;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private int requiredExp;
}
