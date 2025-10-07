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
@Table(name = "todo_definitions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@ToString(exclude = {"owner", "category", "instances"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TodoDefinition extends BaseEntity {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = false, length = 255)
  String title;

  @Column(columnDefinition = "TEXT")
  String description;

  @Column(name = "priority_id")
  Integer priorityId;  // NULL 허용 (0:낮음, 1:보통, 2:높음)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  Category category;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
    name = "todo_definition_tags",
    joinColumns = @JoinColumn(name = "definition_id")
  )
  @Column(name = "tag")
  @Builder.Default
  Set<String> tags = new HashSet<>();

  @Column(name = "recurrence_rule", columnDefinition = "jsonb")
  String recurrenceRule;  // NULL = 1회용 투두

  @Column(name = "base_date")
  LocalDate baseDate;

  @Column(name = "base_time")
  LocalTime baseTime;

  @Column(name = "is_collaborative", nullable = false)
  @Builder.Default
  Boolean isCollaborative = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User owner;

  @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  Set<TodoInstance> instances = new HashSet<>();

  @Column(name = "deleted_at")
  LocalDateTime deletedAt;  // 소프트 삭제

  /**
   * 소프트 삭제
   */
  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
    // 연관된 인스턴스도 소프트 삭제
    this.instances.forEach(TodoInstance::softDelete);
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
   * 반복 투두인지 확인
   */
  public boolean isRecurring() {
    return this.recurrenceRule != null;
  }

  /**
   * 인스턴스 추가
   */
  public void addInstance(TodoInstance instance) {
    this.instances.add(instance);
    instance.setDefinition(this);
  }

  /**
   * 인스턴스 제거
   */
  public void removeInstance(TodoInstance instance) {
    this.instances.remove(instance);
    instance.setDefinition(null);
  }

  /**
   * 접근 권한 확인
   */
  public boolean isAccessibleBy(User user) {
    if (user == null) return false;

    // owner는 항상 접근 가능
    if (this.owner.equals(user)) return true;

    // 협업 투두이고 카테고리가 있는 경우
    if (this.isCollaborative && this.category != null) {
      return this.category.isCollaborator(user);
    }

    return false;
  }

  /**
   * 수정 권한 확인
   */
  public boolean isEditableBy(User user) {
    if (user == null) return false;

    // owner는 항상 수정 가능
    if (this.owner.equals(user)) return true;

    // 협업 투두이고 카테고리가 있는 경우
    if (this.isCollaborative && this.category != null) {
      return this.category.canManage(user);
    }

    return false;
  }
}