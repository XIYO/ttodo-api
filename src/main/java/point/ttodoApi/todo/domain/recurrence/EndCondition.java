package point.ttodoApi.todo.domain.recurrence;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndCondition {
  private EndConditionType type;
  private LocalDate until;
  private Integer count;
}

