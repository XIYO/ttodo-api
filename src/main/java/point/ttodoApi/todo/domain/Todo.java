package point.ttodoApi.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.domain.BaseEntity;
import point.ttodoApi.todo.domain.validation.*;

import java.time.*;
import java.util.*;

import static point.ttodoApi.todo.domain.TodoConstants.*;

@Entity
@Getter
@Setter // 일반 Setter 제공
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수
@AllArgsConstructor(access = AccessLevel.PACKAGE) // MapStruct/테스트용
@Builder
@ToString(exclude = {"owner", "category"}) // 순환참조 방지
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE) // private 자동 적용
public class Todo extends BaseEntity {
  @EqualsAndHashCode.Include
  @EmbeddedId
  TodoId todoId;

  @Column(length = TITLE_MAX_LENGTH)
  @ValidTitle
  String title;
  
  @Column(length = DESCRIPTION_MAX_LENGTH)
  @ValidDescription
  String description;

  @Column(nullable = false)
  @Builder.Default
  Boolean complete = DEFAULT_COMPLETE;

  @Column(name = "is_pinned", nullable = false)
  @Builder.Default
  Boolean isPinned = DEFAULT_IS_PINNED;

  @Column(name = "display_order", nullable = false)
  @ValidDisplayOrder
  @Builder.Default
  Integer displayOrder = DEFAULT_DISPLAY_ORDER;

  @Column(nullable = false)
  @Builder.Default
  Boolean active = DEFAULT_ACTIVE;

  @Column(name = "is_collaborative", nullable = false)
  @Builder.Default
  Boolean isCollaborative = DEFAULT_IS_COLLABORATIVE;

  @ValidTodoPriority
  Integer priorityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  Category category;

  @ValidTodoDate
  LocalDate date;
  
  LocalTime time;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "todo_tags", joinColumns = {
          @JoinColumn(name = "original_todo_id", referencedColumnName = "original_todo_id"),
          @JoinColumn(name = "days_difference", referencedColumnName = "days_difference")
  })
  @Column(name = "tag")
  @ValidTags
  Set<String> tags;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @ValidOwner
  User owner;

  // 롬복 @Builder와 @AllArgsConstructor를 사용하여 자동 생성

  public Long getOriginalTodoId() {
    return todoId != null ? todoId.getId() : null;
  }

  public TodoId getId() {
    return todoId;
  }

  // 불필요한 정적 팩토리 메서드 제거 - Builder가 이미 충분히 명확함

  /**
   * 협업 투두인지 확인
   */
  public boolean isCollaborativeTodo() {
    return this.isCollaborative != null && this.isCollaborative;
  }

  /**
   * 협업 상태 업데이트
   */
  public void updateCollaborativeStatus(boolean isCollaborative) {
    this.isCollaborative = isCollaborative;
  }

  /**
   * 특정 멤버가 이 투두에 접근할 수 있는지 확인
   * owner이거나 카테고리의 협업자인 경우 접근 가능
   */
  public boolean isAccessibleBy(User user) {
    if (user == null) return false;

    // owner는 항상 접근 가능
    if (this.owner.equals(user)) return true;

    // 협업 투두가 아니면 user만 접근 가능
    if (!isCollaborativeTodo()) return false;

    // 카테고리가 없으면 협업 불가
    if (this.category == null) return false;

    // 카테고리의 협업자인지 확인
    return this.category.isCollaborator(user);
  }

  /**
   * 특정 멤버가 이 투두를 수정할 수 있는지 확인
   * owner이거나 카테고리 관리 권한이 있는 경우 수정 가능
   */
  public boolean isEditableBy(User user) {
    if (user == null) return false;

    // owner는 항상 수정 가능
    if (this.owner.equals(user)) return true;

    // 협업 투두가 아니면 user만 수정 가능
    if (!isCollaborativeTodo()) return false;

    // 카테고리가 없으면 협업 불가
    if (this.category == null) return false;

    // 카테고리 관리 권한 확인
    return this.category.canManage(user);
  }

  /**
   * 투두를 협업 투두로 전환
   * 카테고리가 협업 가능한 경우에만 전환 가능
   */
  public void enableCollaboration() {
    if (this.category == null) {
      throw new IllegalStateException("Cannot enable collaboration: Todo must belong to a category");
    }

    if (!this.category.isCollaborative()) {
      throw new IllegalStateException("Cannot enable collaboration: Category has no collaborators");
    }

    this.isCollaborative = true;
  }

  /**
   * 협업 투두를 개인 투두로 전환
   */
  public void disableCollaboration() {
    this.isCollaborative = false;
  }

  /**
   * 투두가 협업 범위에 있는지 확인
   * 카테고리가 있고 해당 카테고리가 협업 가능한 상태인지 확인
   */
  public boolean isInCollaborativeScope() {
    return this.category != null && this.category.isCollaborative();
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

