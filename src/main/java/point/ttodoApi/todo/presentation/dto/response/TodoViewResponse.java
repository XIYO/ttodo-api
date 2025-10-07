package point.ttodoApi.todo.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "투두 통합 뷰 응답")
public class TodoViewResponse {

  @Schema(description = "투두 ID (인스턴스 ID)", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "정의 ID")
  private UUID definitionId;

  @Schema(description = "사용자 ID")
  private UUID userId;

  @Schema(description = "소유자 닉네임", example = "홍길동")
  private String ownerNickname;

  @Schema(description = "제목", example = "오늘의 운동")
  private String title;

  @Schema(description = "설명", example = "30분 이상 유산소 운동")
  private String description;

  @Schema(description = "우선순위 ID (0: 낮음, 1: 보통, 2: 높음)", example = "1")
  private Integer priorityId;

  @Schema(description = "우선순위명", example = "보통")
  private String priorityName;

  @Schema(description = "카테고리 ID")
  private UUID categoryId;

  @Schema(description = "카테고리명", example = "운동")
  private String categoryName;

  @Schema(description = "태그 목록", example = "[\"운동\", \"건강\"]")
  private Set<String> tags;

  @Schema(description = "마감일", example = "2025-01-01")
  private LocalDate dueDate;

  @Schema(description = "마감 시간", example = "09:00:00")
  private LocalTime dueTime;

  @Schema(description = "완료 여부", example = "false")
  private Boolean completed;

  @Schema(description = "완료 시각")
  private LocalDateTime completedAt;

  @Schema(description = "반복 규칙 (JSON)")
  private String recurrenceRule;

  @Schema(description = "반복 투두 여부", example = "true")
  private Boolean isRecurring;

  @Schema(description = "협업 투두 여부", example = "false")
  private Boolean isCollaborative;

  @Schema(description = "고정 여부", example = "false")
  private Boolean isPinned;

  @Schema(description = "활성 상태", example = "true")
  private Boolean isActive;

  @Schema(description = "시퀀스 번호", example = "1")
  private Integer sequenceNumber;

  @Schema(description = "인스턴스 여부 (false면 가상 투두)", example = "true")
  private Boolean hasInstance;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;
}