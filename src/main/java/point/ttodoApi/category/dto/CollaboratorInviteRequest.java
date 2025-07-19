package point.ttodoApi.category.dto;

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
    
    @NotNull(message = "Member ID is required")
    private UUID memberId;
    
    @Size(max = 500, message = "Invitation message must not exceed 500 characters")
    private String invitationMessage;
}