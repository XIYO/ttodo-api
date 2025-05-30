package point.zzicback.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import point.zzicback.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Boolean done;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Todo(Long id, String title, String description, Boolean done, Member member) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.done = done;
        this.member = member;
    }
}
