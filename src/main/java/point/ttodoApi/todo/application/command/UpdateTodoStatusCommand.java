package point.ttodoApi.todo.application.command;

import lombok.Builder;

@Builder
public record UpdateTodoStatusCommand(
    Integer statusId,
    Integer completionRate,
    Integer actualDuration,
    String notes
) {
}