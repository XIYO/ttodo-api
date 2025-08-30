package point.ttodoApi.todo.domain;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.member.domain.Member;

import java.time.*;
import java.util.*;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;
import point.ttodoApi.todo.infrastructure.persistence.converter.RecurrenceRuleJsonConverter;

@Entity
@Table(name = "todo_template")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class TodoTemplate {
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

    // 신규 RRULE 기반 반복 규칙(JSON 직렬화)
    @Convert(converter = RecurrenceRuleJsonConverter.class)
    @Column(name = "recurrence_rule", columnDefinition = "TEXT")
    private RecurrenceRule recurrenceRule;

    // 시리즈 기준일(앵커)
    @Column(name = "anchor_date")
    private LocalDate anchorDate;

    @Column(name = "complete")
    private Boolean complete;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // 구 반복 요일 컬렉션 제거(미래지향: RRULE 사용)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "todo_template_tags", joinColumns = @JoinColumn(name = "todo_template_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member owner;

    @Builder
    public TodoTemplate(
            String title,
            String description,
            Integer priorityId,
            LocalDate date,
            LocalTime time,
            Boolean complete,
            Boolean active,
            Boolean isPinned,
            Integer displayOrder,
            Set<String> tags,
            Category category,
            Member owner
    ) {
        this.title = title;
        this.description = description;
        this.priorityId = priorityId;
        this.date = date;
        this.time = time;
        this.complete = complete;
        this.active = active != null ? active : true;
        this.isPinned = isPinned != null ? isPinned : false;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.tags = tags != null ? tags : new HashSet<>();
        this.category = category;
        this.owner = owner;
    }
    
    
    public void togglePin() {
        this.isPinned = !this.isPinned;
    }
    
    public boolean isCompleted() {
        return complete != null && complete;
    }
    
    /**
     * 소유권 확인 메서드 (Spring Security @PreAuthorize용)
     * @param memberId 확인할 멤버 ID
     * @return 소유자인지 여부
     */
    public boolean isOwn(UUID memberId) {
        if (memberId == null || this.owner == null) return false;
        return this.owner.getId().equals(memberId);
    }
}
