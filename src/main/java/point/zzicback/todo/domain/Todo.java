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
  
  @Column(nullable = false)
  private Integer status = 0;
  
  private Integer priority;
  
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
  public Todo(Long id, String title, String description, Integer status, Integer priority, 
              Category category, LocalDate dueDate, RepeatType repeatType, 
              Set<String> tags, Member member) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.status = status != null ? status : 0;
    this.priority = priority;
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
    return switch (status) {
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
    return status;
  }
}
