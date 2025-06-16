package point.zzicback.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
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
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TodoStatus status = TodoStatus.IN_PROGRESS;
  
  @Enumerated(EnumType.STRING)
  private Priority priority;
  
  @Enumerated(EnumType.STRING)
  private TodoCategory category;
  
  private String customCategory;
  
  private LocalDate dueDate;
  
  @Enumerated(EnumType.STRING)
  private RepeatType repeatType;
  
  @ElementCollection
  @CollectionTable(name = "todo_tags", joinColumns = @JoinColumn(name = "todo_id"))
  @Column(name = "tag")
  private Set<String> tags = new HashSet<>();
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Builder
  public Todo(Long id, String title, String description, TodoStatus status, Priority priority, 
              TodoCategory category, String customCategory, LocalDate dueDate, RepeatType repeatType, 
              Set<String> tags, Member member) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.status = status != null ? status : TodoStatus.IN_PROGRESS;
    this.priority = priority != null ? priority : Priority.MEDIUM;
    this.category = category;
    this.customCategory = customCategory;
    this.dueDate = dueDate;
    this.repeatType = repeatType;
    this.tags = tags != null ? tags : new HashSet<>();
    this.member = member;
  }
  
  @Transient
  public String getDisplayCategory() {
    if (category == null) return null;
    return category == TodoCategory.OTHER ? customCategory : category.getDisplayName();
  }
  
  @Transient
  public String getDisplayPriority() {
    return priority != null ? priority.getDisplayName() : null;
  }
  
  @Transient
  public String getDisplayStatus() {
    return status != null ? status.getDisplayName() : null;
  }
}
