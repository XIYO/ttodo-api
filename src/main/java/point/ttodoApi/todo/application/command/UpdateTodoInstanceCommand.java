package point.ttodoApi.todo.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTodoInstanceCommand {

  // 오버라이드 가능한 필드
  private String title;
  private String description;
  private Integer priorityId;
  private UUID categoryId;
  private Set<String> tags;

  // 인스턴스 고유 필드
  private LocalDate dueDate;
  private LocalTime dueTime;
  private Boolean isPinned;
  private Integer displayOrder;
}