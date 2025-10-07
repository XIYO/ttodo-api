package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "투두 상태 변경 요청")
public class UpdateTodoStatusRequest {

  @Min(value = 0, message = "상태 ID는 0 이상이어야 합니다")
  @Max(value = 3, message = "상태 ID는 3 이하여야 합니다")
  @Schema(description = "상태 ID (0: 예정, 1: 진행중, 2: 완료, 3: 건너뜀)", example = "2", required = true)
  private Integer statusId;

  @Min(value = 0, message = "완료율은 0 이상이어야 합니다")
  @Max(value = 100, message = "완료율은 100 이하여야 합니다")
  @Schema(description = "완료율 (0-100)", example = "100")
  private Integer completionRate;

  @Min(value = 0, message = "실제 소요 시간은 0 이상이어야 합니다")
  @Max(value = 1440, message = "실제 소요 시간은 1440분(24시간) 이하여야 합니다")
  @Schema(description = "실제 소요 시간(분)", example = "25")
  private Integer actualDuration;

  @Size(max = 2000, message = "메모는 2000자 이하여야 합니다")
  @Schema(description = "완료/건너뛰기 메모", example = "성공적으로 완료함")
  private String notes;
}