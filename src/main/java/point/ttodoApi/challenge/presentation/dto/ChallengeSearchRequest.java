package point.ttodoApi.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import point.ttodoApi.shared.dto.BaseSearchRequest;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Challenge 검색 요청 DTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "챌린지 검색 조건")
public class ChallengeSearchRequest extends BaseSearchRequest {

  @Schema(description = "제목 검색 키워드", example = "매일")
  @Size(max = 100, message = "제목 검색어는 100자를 초과할 수 없습니다")
  private String titleKeyword;

  @Schema(description = "설명 검색 키워드", example = "운동")
  @Size(max = 200, message = "설명 검색어는 200자를 초과할 수 없습니다")
  private String descriptionKeyword;

  @Schema(description = "생성자 ID")
  private UUID creatorId;

  @Schema(description = "공개 여부", example = "PUBLIC", allowableValues = {"PUBLIC", "PRIVATE", "FRIENDS_ONLY"})
  private String visibility;

  @Schema(description = "기간 타입", example = "WEEKLY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY", "CUSTOM"})
  private String periodType;

  @Schema(description = "시작일 검색 시작", example = "2024-01-01")
  private LocalDate startDateFrom;

  @Schema(description = "시작일 검색 종료", example = "2024-12-31")
  private LocalDate startDateTo;

  @Schema(description = "종료일 검색 시작", example = "2024-01-01")
  private LocalDate endDateFrom;

  @Schema(description = "종료일 검색 종료", example = "2024-12-31")
  private LocalDate endDateTo;

  @Schema(description = "진행 중인 챌린지만 조회", example = "false")
  private boolean ongoingOnly;

  @Schema(description = "참여 가능한 챌린지만 조회 (최대 인원 미달)", example = "false")
  private boolean joinableOnly;

  @Schema(description = "내가 참여 중인 챌린지만 조회", example = "false")
  private boolean myParticipationOnly;

  @Schema(description = "최소 참여자 수", example = "1")
  @Min(value = 0, message = "최소 참여자 수는 0 이상이어야 합니다")
  private Integer minParticipants;

  @Schema(description = "최대 참여자 수", example = "100")
  @Max(value = 1000, message = "최대 참여자 수는 1000을 초과할 수 없습니다")
  private Integer maxParticipants;

  @Schema(description = "활성 상태만 조회", example = "true")
  @Builder.Default
  private Boolean active = true;

  @Override
  public String defaultSort() {
    return "startDate,desc";
  }

  @Override
  protected void validateBusinessRules() {
    // 날짜 범위 검증
    if (startDateFrom != null && startDateTo != null && startDateTo.isBefore(startDateFrom))
      throw new IllegalArgumentException("시작일 종료 날짜는 시작일 시작 날짜 이후여야 합니다");

    if (endDateFrom != null && endDateTo != null && endDateTo.isBefore(endDateFrom))
      throw new IllegalArgumentException("종료일 종료 날짜는 종료일 시작 날짜 이후여야 합니다");

    // 참여자 수 범위 검증
    if (minParticipants != null && maxParticipants != null && minParticipants > maxParticipants)
      throw new IllegalArgumentException("최소 참여자 수는 최대 참여자 수보다 클 수 없습니다");

    // 진행 중 챌린지 조회 시 날짜 자동 설정
    if (ongoingOnly) {
      LocalDate today = LocalDate.now();
      startDateTo = today;
      endDateFrom = today;
    }
  }

  /**
   * 제목 또는 설명 키워드가 있는지 확인
   */
  public boolean hasKeyword() {
    return hasTitleKeyword() || hasDescriptionKeyword();
  }

  /**
   * 제목 키워드가 있는지 확인
   */
  private boolean hasTitleKeyword() {
    return titleKeyword != null && !titleKeyword.trim().isEmpty();
  }

  /**
   * 설명 키워드가 있는지 확인
   */
  private boolean hasDescriptionKeyword() {
    return descriptionKeyword != null && !descriptionKeyword.trim().isEmpty();
  }

  /**
   * 날짜 필터가 있는지 확인
   */
  public boolean hasDateFilter() {
    return startDateFrom != null || startDateTo != null ||
            endDateFrom != null || endDateTo != null;
  }

  /**
   * 참여자 수 필터가 있는지 확인
   */
  public boolean hasParticipantFilter() {
    return minParticipants != null || maxParticipants != null;
  }
}