package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "투두 정의 수정 요청")
public class UpdateTodoDefinitionRequest {

  @Size(max = 255, message = "제목은 255자 이하여야 합니다")
  @Schema(description = "투두 제목", example = "매일 운동하기")
  private String title;

  @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
  @Schema(description = "투두 설명", example = "30분 이상 유산소 운동")
  private String description;

  @Schema(description = "우선순위 ID (0: 낮음, 1: 보통, 2: 높음)", example = "1")
  private Integer priorityId;

  @Schema(description = "카테고리 ID")
  private UUID categoryId;

  @Schema(description = "태그 목록", example = "[\"운동\", \"건강\"]")
  private Set<String> tags;

  @Schema(description = "반복 규칙 (JSON 형식)",
         example = "{\"frequency\":\"DAILY\",\"interval\":1}")
  private String recurrenceRule;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  @Schema(description = "기준 날짜", example = "2025-01-01")
  private LocalDate baseDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
  @Schema(description = "기준 시간", example = "09:00:00")
  private LocalTime baseTime;

  @Schema(description = "협업 투두 여부")
  private Boolean isCollaborative;

  @Schema(description = "미래 인스턴스도 업데이트할지 여부", defaultValue = "false")
  private Boolean updateFutureInstances;
}