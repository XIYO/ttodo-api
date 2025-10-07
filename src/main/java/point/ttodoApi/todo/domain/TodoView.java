package point.ttodoApi.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.time.*;
import java.util.*;

/**
 * 투두 통합 뷰 (읽기 전용)
 * definition과 instance를 조인하여 효과적인 값을 제공
 */
@Entity
@Immutable  // 읽기 전용
@Subselect("""
  SELECT
    i.id as id,
    i.definition_id as definition_id,
    i.sequence_number as sequence_number,
    i.user_id as user_id,
    COALESCE(i.title, d.title) as title,
    COALESCE(i.description, d.description) as description,
    COALESCE(i.priority_id, d.priority_id) as priority_id,
    COALESCE(i.category_id, d.category_id) as category_id,
    i.due_date as due_date,
    i.due_time as due_time,
    i.completed as completed,
    i.completed_at as completed_at,
    i.is_pinned as is_pinned,
    i.display_order as display_order,
    d.recurrence_rule as recurrence_rule,
    CASE WHEN d.recurrence_rule IS NOT NULL THEN true ELSE false END as is_recurring,
    d.is_collaborative as is_collaborative,
    i.created_at as created_at,
    i.updated_at as updated_at
  FROM todo_instances i
  INNER JOIN todo_definitions d ON i.definition_id = d.id
  WHERE i.deleted_at IS NULL
    AND d.deleted_at IS NULL
""")
@Synchronize({"todo_instances", "todo_definitions"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TodoView {

  @EqualsAndHashCode.Include
  @Id
  UUID id;

  @Column(name = "definition_id")
  UUID definitionId;

  @Column(name = "sequence_number")
  Integer sequenceNumber;

  @Column(name = "user_id")
  UUID userId;

  @Column(length = 255)
  String title;

  @Column(columnDefinition = "TEXT")
  String description;

  @Column(name = "priority_id")
  Integer priorityId;

  @Column(name = "category_id")
  UUID categoryId;

  @Column(name = "due_date")
  LocalDate dueDate;

  @Column(name = "due_time")
  LocalTime dueTime;

  @Column(name = "completed")
  Boolean completed;

  @Column(name = "completed_at")
  LocalDateTime completedAt;

  @Column(name = "is_pinned")
  Boolean isPinned;

  @Column(name = "display_order")
  Integer displayOrder;

  @Column(name = "recurrence_rule", columnDefinition = "jsonb")
  String recurrenceRule;

  @Column(name = "is_recurring")
  Boolean isRecurring;

  @Column(name = "is_collaborative")
  Boolean isCollaborative;

  @Column(name = "created_at")
  LocalDateTime createdAt;

  @Column(name = "updated_at")
  LocalDateTime updatedAt;

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
   * 우선순위 레벨 가져오기
   */
  public String getPriorityLevel() {
    if (this.priorityId == null) return "없음";
    return switch (this.priorityId) {
      case 0 -> "낮음";
      case 1 -> "보통";
      case 2 -> "높음";
      default -> "없음";
    };
  }

  /**
   * 완료 여부 확인
   */
  public boolean isCompleted() {
    return Boolean.TRUE.equals(this.completed);
  }

  /**
   * 고정 여부 확인
   */
  public boolean isPinnedTodo() {
    return Boolean.TRUE.equals(this.isPinned);
  }

  /**
   * 반복 여부 확인
   */
  public boolean isRecurringTodo() {
    return Boolean.TRUE.equals(this.isRecurring);
  }

  /**
   * 협업 여부 확인
   */
  public boolean isCollaborativeTodo() {
    return Boolean.TRUE.equals(this.isCollaborative);
  }
}