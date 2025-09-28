package point.ttodoApi.todo.domain;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;
import point.ttodoApi.todo.domain.validation.*;
import point.ttodoApi.todo.infrastructure.persistence.converter.RecurrenceRuleJsonConverter;

import java.time.*;
import java.util.*;

import static point.ttodoApi.todo.domain.TodoConstants.*;

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

  @Column(nullable = false, length = TITLE_MAX_LENGTH)
  @ValidTitle
  private String title;

  @Column(length = DESCRIPTION_MAX_LENGTH)
  @ValidDescription
  private String description;

  @Column(name = "priority")
  @ValidTodoPriority
  private Integer priorityId;

  @ValidTodoDate(allowPast = true)
  private LocalDate date;

  private LocalTime time;

  // 신규 RRULE 기반 반복 규칙(JSON 직렬화)
  @Convert(converter = RecurrenceRuleJsonConverter.class)
  @Lob
  @Column(name = "recurrence_rule")
  private RecurrenceRule recurrenceRule;

  // 시리즈 기준일(앵커)
  @Column(name = "anchor_date")
  @ValidTodoDate(allowPast = true)
  private LocalDate anchorDate;

  @Column(name = "complete")
  private Boolean complete;

  @Column(name = "active", nullable = false)
  private Boolean active = DEFAULT_ACTIVE;

  @Column(name = "is_pinned", nullable = false)
  private Boolean isPinned = DEFAULT_IS_PINNED;

  @Column(name = "display_order", nullable = false)
  @ValidDisplayOrder
  private Integer displayOrder = DEFAULT_DISPLAY_ORDER;

  @CreatedDate
  @Column(updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  private Instant updatedAt;

  // 구 반복 요일 컬렉션 제거(미래지향: RRULE 사용)

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "todo_template_tags", joinColumns = @JoinColumn(name = "todo_template_id"))
  @Column(name = "tag")
  @ValidTags
  private Set<String> tags = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @ValidOwner
  private User owner;

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
          User owner
  ) {
    this.title = title;
    this.description = description;
    this.priorityId = priorityId;
    this.date = date;
    this.time = time;
    this.complete = complete;
    this.active = active != null ? active : DEFAULT_ACTIVE;
    this.isPinned = isPinned != null ? isPinned : DEFAULT_IS_PINNED;
    this.displayOrder = displayOrder != null ? displayOrder : DEFAULT_DISPLAY_ORDER;
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
   *
   * @param userId 확인할 멤버 ID
   * @return 소유자인지 여부
   */
  public boolean isOwn(UUID userId) {
    if (userId == null || this.owner == null) return false;
    return this.owner.getId().equals(userId);
  }
}
