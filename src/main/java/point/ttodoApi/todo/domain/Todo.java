package point.ttodoApi.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.member.domain.Member;

import java.time.*;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Todo {
  @EmbeddedId
  private TodoId todoId;

  private String title;
  private String description;
  
  @Column(nullable = false)
  private Boolean complete = false;
  
  @Column(name = "is_pinned", nullable = false)
  private Boolean isPinned = false;
  
  @Column(name = "display_order", nullable = false)
  private Integer displayOrder = 0;
  
  @Column(nullable = false)
  private Boolean active = true;
  
  @Column(name = "is_collaborative", nullable = false)
  private Boolean isCollaborative = false;
  
  private Integer priorityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  private LocalDate date;
  private LocalTime time;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "todo_tags", joinColumns = {
      @JoinColumn(name = "original_todo_id", referencedColumnName = "original_todo_id"),
      @JoinColumn(name = "days_difference", referencedColumnName = "days_difference")
  })
  @Column(name = "tag")
  private Set<String> tags;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private Member owner;

  @CreatedDate
  @Column(updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  private Instant updatedAt;

  @Builder
  public Todo(TodoId todoId,
              String title,
              String description,
              Boolean complete,
              Boolean isPinned,
              Integer displayOrder,
              Boolean active,
              Boolean isCollaborative,
              Integer priorityId,
              Category category,
              LocalDate date,
              LocalTime time,
              Set<String> tags,
              Member owner) {
    this.todoId = todoId;
    this.title = title;
    this.description = description;
    this.complete = complete != null ? complete : false;
    this.isPinned = isPinned != null ? isPinned : false;
    this.displayOrder = displayOrder != null ? displayOrder : 0;
    this.active = active != null ? active : true;
    this.isCollaborative = isCollaborative != null ? isCollaborative : false;
    this.priorityId = priorityId;
    this.category = category;
    this.date = date;
    this.time = time;
    this.tags = tags;
    this.owner = owner;
  }

  public Long getOriginalTodoId() {
    return todoId != null ? todoId.getId() : null;
  }
  
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
  public boolean isAccessibleBy(Member member) {
    if (member == null) return false;
    
    // owner는 항상 접근 가능
    if (this.owner.equals(member)) return true;
    
    // 협업 투두가 아니면 owner만 접근 가능
    if (!isCollaborativeTodo()) return false;
    
    // 카테고리가 없으면 협업 불가
    if (this.category == null) return false;
    
    // 카테고리의 협업자인지 확인
    return this.category.isCollaborator(member);
  }
  
  /**
   * 특정 멤버가 이 투두를 수정할 수 있는지 확인
   * owner이거나 카테고리 관리 권한이 있는 경우 수정 가능
   */
  public boolean isEditableBy(Member member) {
    if (member == null) return false;
    
    // owner는 항상 수정 가능
    if (this.owner.equals(member)) return true;
    
    // 협업 투두가 아니면 owner만 수정 가능
    if (!isCollaborativeTodo()) return false;
    
    // 카테고리가 없으면 협업 불가
    if (this.category == null) return false;
    
    // 카테고리 관리 권한 확인
    return this.category.canManage(member);
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
   * @param memberId 확인할 멤버 ID
   * @return 소유자인지 여부
   */
  public boolean isOwn(UUID memberId) {
    if (memberId == null || this.owner == null) return false;
    return this.owner.getId().equals(memberId);
  }
}

