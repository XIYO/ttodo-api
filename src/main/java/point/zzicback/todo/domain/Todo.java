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
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

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
              Integer priorityId,
              Category category,
              LocalDate date,
              LocalTime time,
              Set<String> tags,
              Member member) {
    this.todoId = todoId;
    this.title = title;
    this.description = description;
    this.complete = complete != null ? complete : false;
    this.priorityId = priorityId;
    this.category = category;
    this.date = date;
    this.time = time;
    this.tags = tags;
    this.member = member;
  }

  public Long getOriginalTodoId() {
    return todoId != null ? todoId.getId() : null;
  }
}

