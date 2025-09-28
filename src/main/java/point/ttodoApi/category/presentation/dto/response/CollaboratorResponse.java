package point.ttodoApi.category.presentation.dto.response;

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
  private UUID userId;
  private String userNickname;
  private String userEmail;
  private CollaboratorStatus status;
  private LocalDateTime invitedAt;
  private LocalDateTime acceptedAt;
  private String invitationMessage;

  // CollaboratorResponse는 서비스 레이어에서 Profile 정보와 함께 구성되어야 함
  public static CollaboratorResponse from(CategoryCollaborator collaborator, String userNickname) {
    return CollaboratorResponse.builder()
            .id(collaborator.getId())
            .categoryId(collaborator.getCategory().getId())
            .categoryName(collaborator.getCategory().getName())
            .userId(collaborator.getUser().getId())
            .userNickname(userNickname)  // Profile.nickname을 매개변수로 받음
            .userEmail(collaborator.getUser().getEmail())
            .status(collaborator.getStatus())
            .invitedAt(collaborator.getInvitedAt())
            .acceptedAt(collaborator.getAcceptedAt())
            .invitationMessage(collaborator.getInvitationMessage())
            .build();
  }
}