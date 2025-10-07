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
@Schema(description = "투두 인스턴스 응답")
public class TodoInstanceResponse {

  @Schema(description = "인스턴스 ID", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "정의 ID")
  private UUID definitionId;

  @Schema(description = "시퀀스 번호", example = "1")
  private Integer sequenceNumber;

  @Schema(description = "재정의된 제목 (없으면 정의의 제목 사용)", example = "오늘의 운동")
  private String title;

  @Schema(description = "재정의된 설명", example = "40분 런닝")
  private String description;

  @Schema(description = "재정의된 우선순위", example = "2")
  private Integer priorityId;

  @Schema(description = "재정의된 카테고리 ID")
  private UUID categoryId;

  @Schema(description = "재정의된 태그 목록")
  private Set<String> tags;

  @Schema(description = "마감일", example = "2025-01-01")
  private LocalDate dueDate;

  @Schema(description = "마감 시간", example = "09:00:00")
  private LocalTime dueTime;

  @Schema(description = "완료 여부", example = "false")
  private Boolean completed;

  @Schema(description = "완료 시각")
  private LocalDateTime completedAt;

  @Schema(description = "고정 여부", example = "false")
  private Boolean isPinned;

  @Schema(description = "표시 순서", example = "0")
  private Integer displayOrder;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  @Schema(description = "삭제일시")
  private LocalDateTime deletedAt;

  // 정의에서 가져온 정보
  @Schema(description = "기본 제목 (정의)", example = "매일 운동하기")
  private String definitionTitle;

  @Schema(description = "기본 설명 (정의)")
  private String definitionDescription;

  @Schema(description = "기본 우선순위 (정의)")
  private Integer definitionPriorityId;

  @Schema(description = "카테고리명")
  private String categoryName;

  @Schema(description = "반복 투두 여부")
  private Boolean isRecurring;
}