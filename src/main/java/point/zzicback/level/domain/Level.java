package point.zzicback.level.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "levels")
public class Level {
    @Id
    private Integer level;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int requiredExp;

    @Builder
    public Level(Integer level, String name, int requiredExp) {
        this.level = level;
        this.name = name;
        this.requiredExp = requiredExp;
    }
}
