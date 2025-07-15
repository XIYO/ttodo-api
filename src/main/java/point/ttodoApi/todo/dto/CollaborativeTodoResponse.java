package point.ttodoApi.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import point.ttodoApi.todo.domain.Todo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

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
    private UUID memberId;
    private String memberNickname;
    private LocalDate date;
    private LocalTime time;
    private Set<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean canEdit;
    
    public static CollaborativeTodoResponse from(Todo todo, boolean canEdit) {
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
            .memberId(todo.getMember().getId())
            .memberNickname(todo.getMember().getNickname())
            .date(todo.getDate())
            .time(todo.getTime())
            .tags(todo.getTags())
            .createdAt(todo.getCreatedAt())
            .updatedAt(todo.getUpdatedAt())
            .canEdit(canEdit)
            .build();
    }
}