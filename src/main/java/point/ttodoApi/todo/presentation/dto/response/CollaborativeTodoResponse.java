package point.ttodoApi.todo.presentation.dto.response;

import lombok.*;
import point.ttodoApi.todo.domain.Todo;

import java.time.*;
import java.util.*;

/**
 * 협업 투두 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborativeTodoResponse {

  private Long originalTodoId;
  private Integer daysDifference;
  private String title;
  private String description;
  private Boolean complete;
  private Boolean isPinned;
  private Integer displayOrder;
  private Boolean isCollaborative;
  private Integer priorityId;
  private UUID categoryId;
  private String categoryName;
  private UUID ownerId;
  private String ownerNickname;
  private LocalDate date;
  private LocalTime time;
  private Set<String> tags;
  private Instant createdAt;
  private Instant updatedAt;
  private Boolean canEdit;

  public static CollaborativeTodoResponse from(Todo todo, String ownerNickname, boolean canEdit) {
    return CollaborativeTodoResponse.builder()
            .originalTodoId(todo.getTodoId().getId())
            .daysDifference(todo.getTodoId().getDaysDifference().intValue())
            .title(todo.getTitle())
            .description(todo.getDescription())
            .complete(todo.getComplete())
            .isPinned(todo.getIsPinned())
            .displayOrder(todo.getDisplayOrder())
            .isCollaborative(todo.getIsCollaborative())
            .priorityId(todo.getPriorityId())
            .categoryId(todo.getCategory() != null ? todo.getCategory().getId() : null)
            .categoryName(todo.getCategory() != null ? todo.getCategory().getName() : null)
            .ownerId(todo.getOwner().getId())
            .ownerNickname(ownerNickname)  // Profile에서 가져온 nickname을 매개변수로 받음
            .date(todo.getDate())
            .time(todo.getTime())
            .tags(todo.getTags())
            .createdAt(todo.getCreatedAt())
            .updatedAt(todo.getUpdatedAt())
            .canEdit(canEdit)
            .build();
  }
}