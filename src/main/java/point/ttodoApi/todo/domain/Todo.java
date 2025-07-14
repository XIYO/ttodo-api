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
}

