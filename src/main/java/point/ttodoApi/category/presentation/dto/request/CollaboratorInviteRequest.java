package point.ttodoApi.category.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * 협업자 초대 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorInviteRequest {

  private UUID userId;

  @Size(max = 500, message = "Invitation message must not exceed 500 characters")
  private String invitationMessage;
}