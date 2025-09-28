package point.ttodoApi.todo.application.result;

import point.ttodoApi.todo.domain.Todo;

import java.time.*;
import java.util.*;

/**
 * 협업 투두 결과 DTO (Application Layer)
 */
public record CollaborativeTodoResult(
        Long originalTodoId,
        Integer daysDifference,
        String title,
        String description,
        Boolean complete,
        Boolean isPinned,
        Integer displayOrder,
        Boolean isCollaborative,
        Integer priorityId,
        UUID categoryId,
        String categoryName,
        UUID ownerId,
        String ownerNickname,
        LocalDate date,
        LocalTime time,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt,
        Boolean canEdit
) {

  public static CollaborativeTodoResult from(Todo todo, String ownerNickname, boolean canEdit) {
    return new CollaborativeTodoResult(
            todo.getTodoId().getId(),
            todo.getTodoId().getDaysDifference().intValue(),
            todo.getTitle(),
            todo.getDescription(),
            todo.getComplete(),
            todo.getIsPinned(),
            todo.getDisplayOrder(),
            todo.getIsCollaborative(),
            todo.getPriorityId(),
            todo.getCategory() != null ? todo.getCategory().getId() : null,
            todo.getCategory() != null ? todo.getCategory().getName() : null,
            todo.getOwner().getId(),
            ownerNickname,  // Profile에서 가져온 nickname을 매개변수로 받음
            todo.getDate(),
            todo.getTime(),
            todo.getTags(),
            todo.getCreatedAt(),
            todo.getUpdatedAt(),
            canEdit
    );
  }
}