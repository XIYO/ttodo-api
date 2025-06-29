package point.zzicback.category.domain;

import jakarta.persistence.*;
import lombok.*;
import point.zzicback.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Builder
    public Category(String name, String color, String description, Member member) {
        this.name = name;
        this.color = color;
        this.description = description;
        this.member = member;
    }

    public void update(String name, String color, String description) {
        this.name = name;
        this.color = color;
        this.description = description;
    }
}
