package point.ttodoApi.challenge.presentation.dto.request;

import lombok.*;

import java.util.UUID;

/**
 * 리더 임명 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderAppointRequest {

  private UUID userId;
}