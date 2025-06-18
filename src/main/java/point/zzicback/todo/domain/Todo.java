package point.zzicback.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import point.zzicback.category.domain.Category;
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
  
  @Column(name = "status", nullable = false)
  private Integer statusId = 0;
  
  @Column(name = "priority")
  private Integer priorityId;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;
  
  private LocalDate dueDate;
  
  @Enumerated(EnumType.ORDINAL)
  private RepeatType repeatType;
  
  @ElementCollection
  @CollectionTable(name = "todo_tags", joinColumns = @JoinColumn(name = "todo_id"))
  @Column(name = "tag")
  private Set<String> tags = new HashSet<>();
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Builder
  public Todo(Long id, String title, String description, Integer statusId, Integer priorityId, 
              Category category, LocalDate dueDate, RepeatType repeatType, 
              Set<String> tags, Member member) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.statusId = statusId != null ? statusId : 0;
    this.priorityId = priorityId;
    this.category = category;
    this.dueDate = dueDate;
    this.repeatType = repeatType;
    this.tags = tags != null ? tags : new HashSet<>();
    this.member = member;
  }
  
  @Transient
  public String getDisplayCategory() {
    return category != null ? category.getName() : null;
  }
  
  @Transient
  public String getDisplayStatus() {
    return switch (statusId) {
      case 0 -> "진행중";
      case 1 -> "완료";
      case 2 -> "지연";
      default -> "알 수 없음";
    };
  }
  
  @Transient
  public String getActualDisplayStatus() {
    return switch (getActualStatus()) {
      case 0 -> "진행중";
      case 1 -> "완료";
      case 2 -> "지연";
      default -> "알 수 없음";
    };
  }
  
  @Transient
  public Integer getActualStatus() {
    return statusId;
  }
}
