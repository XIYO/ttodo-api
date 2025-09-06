package point.ttodoApi.category.presentation.dto;

import lombok.*;
import point.ttodoApi.category.domain.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 협업자 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaboratorResponse {

  private Long id;
  private UUID categoryId;
  private String categoryName;
  private UUID memberId;
  private String memberNickname;
  private String memberEmail;
  private CollaboratorStatus status;
  private LocalDateTime invitedAt;
  private LocalDateTime acceptedAt;
  private String invitationMessage;

  public static CollaboratorResponse from(CategoryCollaborator collaborator) {
    return CollaboratorResponse.builder()
            .id(collaborator.getId())
            .categoryId(collaborator.getCategory().getId())
            .categoryName(collaborator.getCategory().getName())
            .memberId(collaborator.getMember().getId())
            .memberNickname(collaborator.getMember().getNickname())
            .memberEmail(collaborator.getMember().getEmail())
            .status(collaborator.getStatus())
            .invitedAt(collaborator.getInvitedAt())
            .acceptedAt(collaborator.getAcceptedAt())
            .invitationMessage(collaborator.getInvitationMessage())
            .build();
  }
}