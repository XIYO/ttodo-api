package point.ttodoApi.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.*;

/**
 * 날짜 범위 검색을 위한 공통 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "날짜 범위 검색 조건")
public class DateRangeRequest {

  @Schema(description = "시작 날짜", example = "2024-01-01")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @Schema(description = "종료 날짜", example = "2024-12-31")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  @Schema(description = "시작 일시", example = "2024-01-01T00:00:00")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime startDateTime;

  @Schema(description = "종료 일시", example = "2024-12-31T23:59:59")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime endDateTime;

  /**
   * 날짜 범위가 유효한지 검증
   */
  @AssertTrue(message = "종료 날짜는 시작 날짜 이후여야 합니다")
  public boolean isValidDateRange() {
    if (startDate != null && endDate != null) {
      return !endDate.isBefore(startDate);
    }
    return true;
  }

  /**
   * 일시 범위가 유효한지 검증
   */
  @AssertTrue(message = "종료 일시는 시작 일시 이후여야 합니다")
  public boolean isValidDateTimeRange() {
    if (startDateTime != null && endDateTime != null) {
      return !endDateTime.isBefore(startDateTime);
    }
    return true;
  }

  /**
   * 날짜 범위가 설정되어 있는지 확인
   */
  public boolean hasDateRange() {
    return startDate != null || endDate != null;
  }

  /**
   * 일시 범위가 설정되어 있는지 확인
   */
  public boolean hasDateTimeRange() {
    return startDateTime != null || endDateTime != null;
  }

  /**
   * 시작 일시 반환 (날짜만 있는 경우 00:00:00으로 변환)
   */
  public LocalDateTime getEffectiveStartDateTime() {
    if (startDateTime != null) {
      return startDateTime;
    }
    if (startDate != null) {
      return startDate.atStartOfDay();
    }
    return null;
  }

  /**
   * 종료 일시 반환 (날짜만 있는 경우 23:59:59로 변환)
   */
  public LocalDateTime getEffectiveEndDateTime() {
    if (endDateTime != null) {
      return endDateTime;
    }
    if (endDate != null) {
      return endDate.atTime(23, 59, 59);
    }
    return null;
  }

  /**
   * 특정 날짜가 범위 내에 있는지 확인
   */
  public boolean isInRange(LocalDate date) {
    if (date == null) {
      return false;
    }

    boolean afterStart = startDate == null || !date.isBefore(startDate);
    boolean beforeEnd = endDate == null || !date.isAfter(endDate);

    return afterStart && beforeEnd;
  }

  /**
   * 특정 일시가 범위 내에 있는지 확인
   */
  public boolean isInRange(LocalDateTime dateTime) {
    if (dateTime == null) {
      return false;
    }

    LocalDateTime effectiveStart = getEffectiveStartDateTime();
    LocalDateTime effectiveEnd = getEffectiveEndDateTime();

    boolean afterStart = effectiveStart == null || !dateTime.isBefore(effectiveStart);
    boolean beforeEnd = effectiveEnd == null || !dateTime.isAfter(effectiveEnd);

    return afterStart && beforeEnd;
  }
}