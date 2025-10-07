package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "투두 인스턴스 수정 요청")
public class UpdateTodoInstanceRequest {

  @Size(max = 255, message = "제목은 255자 이하여야 합니다")
  @Schema(description = "재정의된 제목", example = "오늘의 운동")
  private String title;

  @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
  @Schema(description = "재정의된 설명", example = "40분 런닝")
  private String description;

  @Schema(description = "재정의된 우선순위 (0: 낮음, 1: 보통, 2: 높음)")
  private Integer priorityId;

  @Schema(description = "재정의된 카테고리 ID")
  private UUID categoryId;

  @Schema(description = "재정의된 태그")
  private Set<String> tags;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  @Schema(description = "예정일", example = "2025-01-01")
  private LocalDate scheduledDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
  @Schema(description = "예정 시간", example = "09:00:00")
  private LocalTime scheduledTime;

  @Min(value = 0, message = "추정 소요 시간은 0 이상이어야 합니다")
  @Max(value = 1440, message = "추정 소요 시간은 1440분(24시간) 이하여야 합니다")
  @Schema(description = "추정 소요 시간(분)", example = "30")
  private Integer estimatedDuration;

  @Size(max = 2000, message = "메모는 2000자 이하여야 합니다")
  @Schema(description = "메모", example = "날씨 좋으면 공원에서")
  private String notes;

  @Schema(description = "고정 여부")
  private Boolean isPinned;
}