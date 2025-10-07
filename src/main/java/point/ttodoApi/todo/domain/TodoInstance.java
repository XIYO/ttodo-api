package point.ttodoApi.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.domain.BaseEntity;

import java.time.*;
import java.util.*;

@Entity
@Table(
  name = "todo_instances",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"definition_id", "sequence_number"})
  }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@ToString(exclude = {"definition", "owner", "category"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TodoInstance extends BaseEntity {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "definition_id", nullable = false)
  TodoDefinition definition;

  @Column(name = "sequence_number", nullable = false)
  @Builder.Default
  Integer sequenceNumber = 1;  // 1회용은 항상 1

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User owner;

  // 오버라이드 가능한 필드들 (NULL이면 definition의 값 사용)
  @Column(length = 255)
  String title;

  @Column(columnDefinition = "TEXT")
  String description;

  @Column(name = "priority_id")
  Integer priorityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  Category category;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
    name = "todo_instance_tags",
    joinColumns = @JoinColumn(name = "instance_id")
  )
  @Column(name = "tag")
  Set<String> tags;

  // 인스턴스 고유 필드
  @Column(name = "due_date", nullable = false)
  LocalDate dueDate;

  @Column(name = "due_time")
  LocalTime dueTime;

  @Column(name = "completed", nullable = false)
  @Builder.Default
  Boolean completed = false;

  @Column(name = "completed_at")
  LocalDateTime completedAt;

  @Column(name = "is_pinned", nullable = false)
  @Builder.Default
  Boolean isPinned = false;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  Integer displayOrder = 0;

  @Column(name = "deleted_at")
  LocalDateTime deletedAt;  // 소프트 삭제

  /**
   * 소프트 삭제
   */
  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  /**
   * 삭제 취소
   */
  public void restore() {
    this.deletedAt = null;
  }

  /**
   * 삭제 여부 확인
   */
  public boolean isDeleted() {
    return this.deletedAt != null;
  }

  /**
   * 완료 처리
   */
  public void markComplete() {
    this.completed = true;
    this.completedAt = LocalDateTime.now();
  }

  /**
   * 완료 취소
   */
  public void markIncomplete() {
    this.completed = false;
    this.completedAt = null;
  }

  /**
   * 효과적인 제목 가져오기 (오버라이드 우선)
   */
  public String getEffectiveTitle() {
    return this.title != null ? this.title : this.definition.getTitle();
  }

  /**
   * 효과적인 설명 가져오기 (오버라이드 우선)
   */
  public String getEffectiveDescription() {
    return this.description != null ? this.description : this.definition.getDescription();
  }

  /**
   * 효과적인 우선순위 가져오기 (오버라이드 우선)
   */
  public Integer getEffectivePriorityId() {
    return this.priorityId != null ? this.priorityId : this.definition.getPriorityId();
  }

  /**
   * 효과적인 카테고리 가져오기 (오버라이드 우선)
   */
  public Category getEffectiveCategory() {
    return this.category != null ? this.category : this.definition.getCategory();
  }

  /**
   * 효과적인 태그 가져오기 (오버라이드 우선)
   */
  public Set<String> getEffectiveTags() {
    return this.tags != null ? this.tags : this.definition.getTags();
  }

  /**
   * 접근 권한 확인
   */
  public boolean isAccessibleBy(User user) {
    if (user == null) return false;

    // owner는 항상 접근 가능
    if (this.owner.equals(user)) return true;

    // definition의 접근 권한 확인
    return this.definition.isAccessibleBy(user);
  }

  /**
   * 수정 권한 확인
   */
  public boolean isEditableBy(User user) {
    if (user == null) return false;

    // owner는 항상 수정 가능
    if (this.owner.equals(user)) return true;

    // definition의 수정 권한 확인
    return this.definition.isEditableBy(user);
  }

  /**
   * 오늘인지 확인
   */
  public boolean isToday() {
    return this.dueDate != null && this.dueDate.equals(LocalDate.now());
  }

  /**
   * 지난 일정인지 확인
   */
  public boolean isOverdue() {
    if (Boolean.TRUE.equals(this.completed)) return false;
    return this.dueDate != null && this.dueDate.isBefore(LocalDate.now());
  }

  /**
   * 예정된 일정인지 확인
   */
  public boolean isUpcoming() {
    if (Boolean.TRUE.equals(this.completed)) return false;
    return this.dueDate != null && this.dueDate.isAfter(LocalDate.now());
  }

  /**
   * 완료 여부 확인
   */
  public boolean isCompleted() {
    return Boolean.TRUE.equals(this.completed);
  }
}