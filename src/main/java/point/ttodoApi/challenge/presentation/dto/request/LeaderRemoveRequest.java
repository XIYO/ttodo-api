package point.ttodoApi.challenge.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 리더 해제 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderRemoveRequest {
    
    @Size(max = 500, message = "Removal reason must not exceed 500 characters")
    private String reason;
}