package point.zzicback.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import point.zzicback.member.domain.Member;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class RepeatTodo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;
    
    @Column(name = "repeat_type", nullable = false)
    private Integer repeatType;
    
    @Column(name = "repeat_interval")
    private Integer repeatInterval;
    
    @Column(name = "repeat_start_date")
    private LocalDate repeatStartDate;
    
    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "repeat_todo_days_of_week", joinColumns = @JoinColumn(name = "repeat_todo_id"))
    @Column(name = "day_of_week")
    private Set<Integer> daysOfWeek = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
    
    @Builder
    public RepeatTodo(Todo todo, Integer repeatType, Integer repeatInterval, 
                     LocalDate repeatStartDate, LocalDate repeatEndDate, Member member, Boolean isActive, Set<Integer> daysOfWeek) {
        this.todo = todo;
        this.repeatType = repeatType;
        this.repeatInterval = repeatInterval;
        this.repeatStartDate = repeatStartDate;
        this.repeatEndDate = repeatEndDate;
        this.member = member;
        this.isActive = isActive != null ? isActive : true;
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new HashSet<>();
    }
}
