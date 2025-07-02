package point.zzicback.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import point.zzicback.category.domain.Category;
import point.zzicback.member.domain.Member;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "todo_original")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class TodoOriginal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "priority")
    private Integer priorityId;

    private LocalDate date;

    private LocalTime time;

    @Column(name = "repeat_type", nullable = false)
    private Integer repeatType;

    @Column(name = "repeat_interval")
    private Integer repeatInterval;

    @Column(name = "repeat_start_date")
    private LocalDate repeatStartDate;

    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;

    @Column(name = "complete", nullable = false)
    private Boolean complete = false;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "todo_original_days_of_week", joinColumns = @JoinColumn(name = "todo_original_id"))
    @Column(name = "day_of_week")
    private Set<Integer> daysOfWeek = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "todo_original_tags", joinColumns = @JoinColumn(name = "todo_original_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public TodoOriginal(
            String title,
            String description,
            Integer priorityId,
            LocalDate date,
            LocalTime time,
            Integer repeatType,
            Integer repeatInterval,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate,
            Boolean complete,
            Set<Integer> daysOfWeek,
            Set<String> tags,
            Category category,
            Member member
    ) {
        this.title = title;
        this.description = description;
        this.priorityId = priorityId;
        this.date = date;
        this.time = time;
        this.repeatType = repeatType;
        this.repeatInterval = repeatInterval;
        this.repeatStartDate = repeatStartDate;
        this.repeatEndDate = repeatEndDate;
        this.complete = complete != null ? complete : false;
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new HashSet<>();
        this.tags = tags != null ? tags : new HashSet<>();
        this.category = category;
        this.member = member;
    }
    
    public boolean isCompleted() {
        return complete != null && complete;
    }
}
