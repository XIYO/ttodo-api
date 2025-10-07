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
@Schema(description = "투두 정의 응답")
public class TodoDefinitionResponse {

  @Schema(description = "정의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "제목", example = "매일 운동하기")
  private String title;

  @Schema(description = "설명", example = "30분 이상 유산소 운동")
  private String description;

  @Schema(description = "우선순위 ID (0: 낮음, 1: 보통, 2: 높음)", example = "1")
  private Integer priorityId;

  @Schema(description = "카테고리 ID")
  private UUID categoryId;

  @Schema(description = "카테고리명", example = "운동")
  private String categoryName;

  @Schema(description = "태그 목록", example = "[\"운동\", \"건강\"]")
  private Set<String> tags;

  @Schema(description = "반복 규칙 (JSON 형식)")
  private String recurrenceRule;

  @Schema(description = "반복 투두 여부", example = "true")
  private Boolean isRecurring;

  @Schema(description = "기준 날짜", example = "2025-01-01")
  private LocalDate baseDate;

  @Schema(description = "기준 시간", example = "09:00:00")
  private LocalTime baseTime;

  @Schema(description = "협업 투두 여부", example = "false")
  private Boolean isCollaborative;

  @Schema(description = "활성 상태", example = "true")
  private Boolean isActive;

  @Schema(description = "소유자 ID")
  private UUID ownerId;

  @Schema(description = "소유자 닉네임", example = "홍길동")
  private String ownerNickname;

  @Schema(description = "인스턴스 수", example = "10")
  private Integer instanceCount;

  @Schema(description = "완료된 인스턴스 수", example = "3")
  private Integer completedInstanceCount;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  @Schema(description = "삭제일시 (소프트 삭제)")
  private LocalDateTime deletedAt;
}