package point.ttodoApi.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.ttodoApi.todo.domain.recurrence.*;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "RRULE 기반 반복 규칙")
public record RecurrenceRuleRequest(
        @Schema(description = "반복 주기", example = "WEEKLY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY", "YEARLY"})
        Frequency frequency,
        @Schema(description = "간격(기본 1)", example = "2")
        Integer interval,
        @Schema(description = "요일 선택(WEEKLY)", example = "[\"MO\",\"WE\",\"FR\"]")
        Set<WeekDay> byWeekDays,
        @Schema(description = "월별 특정 일자(MONTHLY)", example = "[15]")
        Set<Integer> byMonthDay,
        @Schema(description = "월별 n번째 요일 위치(1..5,-1)", example = "[1]")
        Set<Integer> bySetPos,
        @Schema(description = "연간 특정 월(1..12)", example = "[6]")
        Set<Integer> byMonth,
        @Schema(description = "주 시작 요일", example = "MO")
        WeekDay weekStart,
        @Schema(description = "종료 조건")
        EndConditionRequest endCondition,
        @Schema(description = "제외 날짜", example = "[\"2025-06-15\"]")
        Set<LocalDate> exDates,
        @Schema(description = "추가 날짜", example = "[\"2025-06-22\"]")
        Set<LocalDate> rDates,
        @Schema(description = "타임존(IANA)", example = "Asia/Seoul")
        String timezone,
        @Schema(description = "시리즈 기준일", example = "2025-06-01")
        LocalDate anchorDate
) {
}

