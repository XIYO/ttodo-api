package point.ttodoApi.challenge.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * 리더 임명 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderAppointRequest {
    
    @NotNull(message = "Member ID is required")
    private UUID memberId;
}