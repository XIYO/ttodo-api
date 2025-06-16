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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Builder
    public Category(String name, Member member) {
        this.name = name;
        this.member = member;
    }
    
    public void updateName(String name) {
        this.name = name;
    }
}
