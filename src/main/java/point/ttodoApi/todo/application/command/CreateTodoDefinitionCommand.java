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
public class CreateTodoDefinitionCommand {

  private String title;
  private String description;
  private Integer priorityId;
  private UUID categoryId;
  private Set<String> tags;
  private String recurrenceRule;  // JSON string for recurrence rule
  private LocalDate baseDate;
  private LocalTime baseTime;
  private Boolean isCollaborative;
}