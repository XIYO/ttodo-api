package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import point.ttodoApi.todo.presentation.validation.ValidCompleteUpdate;

import java.time.*;
import java.util.*;

@ValidCompleteUpdate
@Schema(description = "Todo 수정 요청 DTO")
public record UpdateTodoRequest(
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다") 
    @Schema(
        description = "할일 제목", 
        example = "영어 공부하기",
        maxLength = 255
    )
    String title,
    
    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다") 
    @Schema(
        description = "할일 설명", 
        example = "토익 문제집 2장 풀기",
        maxLength = 1000
    )
    String description,
    
    @Schema(
        description = "완료 여부", 
        example = "false"
    )
    Boolean complete,
    
    @Schema(
        description = "우선순위 (0: 낮음, 1: 보통, 2: 높음)", 
        example = "1", 
        allowableValues = {"0", "1", "2"},
        minimum = "0",
        maximum = "2"
    )
    Integer priorityId,
    
    @Schema(
        description = "카테고리 ID", 
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID categoryId,

    @Schema(
        description = "마감 날짜",
        example = "2026-01-01",
        format = "date"
    )
    LocalDate date,

    @Schema(
        description = "마감 시간",
        example = "18:00",
        format = "time"
    )
    LocalTime time,

    @Schema(description = "RRULE 기반 반복 규칙 - JSON 문자열로 전송", example = "{\"frequency\":\"WEEKLY\",\"interval\":1,\"byWeekDays\":[\"MO\",\"WE\",\"FR\"]}")
    String recurrenceRuleJson,
    
    @Schema(
        description = "태그 목록",
        type = "array",
        example = "[\"공부\", \"영어\"]"
    )
    Set<String> tags
) {
}