package point.ttodoApi.category.domain;

import jakarta.persistence.*;
import lombok.*;
import point.ttodoApi.member.domain.Member;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;
    
    @Builder
    public Category(String name, String color, String description, Member owner) {
        this.name = name;
        this.color = color;
        this.description = description;
        this.owner = owner;
    }

    public void update(String name, String color, String description) {
        this.name = name;
        this.color = color;
        this.description = description;
    }
}
