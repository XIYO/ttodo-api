package point.ttodoApi.todo.application.command;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record CreateTodoInstanceCommand(
    UUID definitionId,
    Integer sequenceNumber,
    String title,
    String description,
    Integer priorityId,
    UUID categoryId,
    Set<String> tags,
    LocalDate scheduledDate,
    LocalTime scheduledTime,
    Integer estimatedDuration,
    String notes,
    Boolean isPinned
) {
}