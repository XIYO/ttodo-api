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
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Todo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false)
  private String title;
  
  private String description;
  
  @Column(name = "status", nullable = false)
  private Integer statusId = 0;
  
  @Column(name = "priority")
  private Integer priorityId;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;
  
  private LocalDate dueDate;

  private LocalTime dueTime;
  
  @Enumerated(EnumType.ORDINAL)
  private RepeatType repeatType;
  
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "todo_tags", joinColumns = @JoinColumn(name = "todo_id"))
  @Column(name = "tag")
  private Set<String> tags = new HashSet<>();
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @CreatedDate
  @Column(updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  private Instant updatedAt;

  @Builder
  public Todo(Long id, String title, String description, Integer statusId, Integer priorityId,
              Category category, LocalDate dueDate, LocalTime dueTime, RepeatType repeatType,
              Set<String> tags, Member member) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.statusId = statusId != null ? statusId : 0;
    this.priorityId = priorityId;
    this.category = category;
    this.dueDate = dueDate;
    this.dueTime = dueTime;
    this.repeatType = repeatType;
    this.tags = tags != null ? tags : new HashSet<>();
    this.member = member;
  }

  @Transient
  public Integer getActualStatus() {
    return statusId;
  }
}
