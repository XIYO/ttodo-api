package point.ttodoApi.challenge.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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