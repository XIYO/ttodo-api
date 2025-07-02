package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Todo 수정 요청 DTO")
public class UpdateTodoRequest {

    @Size(max = 255) 
    @Schema(
        description = "할일 제목", 
        example = "영어 공부하기",
        maxLength = 255
    )
    private String title;
    
    @Size(max = 1000) 
    @Schema(
        description = "할일 설명", 
        example = "토익 문제집 2장 풀기",
        maxLength = 1000
    )
    private String description;
    
    @Schema(
        description = "완료 여부", 
        example = "false"
    )
    private Boolean complete;
    
    @Schema(
        description = "우선순위 (0: 낮음, 1: 보통, 2: 높음)", 
        example = "1", 
        allowableValues = {"0", "1", "2"},
        minimum = "0",
        maximum = "2"
    )
    private Integer priorityId;
    
    @Schema(
        description = "카테고리 ID", 
        example = "1",
        minimum = "1"
    )
    private Long categoryId;

    @Schema(
        description = "마감 날짜",
        example = "2026-01-01",
        format = "date"
    )
    private LocalDate date;

    @Schema(
        description = "마감 시간",
        example = "18:00",
        format = "time"
    )
    private LocalTime time;
    
    @Schema(
        description = "반복 유형 (0: 반복 안함, 1: 데일리, 2: 위클리, 3: 먼슬리, 4: 이얼리)", 
        example = "0", 
        allowableValues = {"0", "1", "2", "3", "4"}
    )
    private Integer repeatType;
    
    @Schema(
        description = "반복 간격 (일 단위)", 
        example = "1"
    )
    private Integer repeatInterval;
    
    @Schema(
        description = "반복 시작일",
        example = "2026-01-01",
        format = "date"
    )
    private LocalDate repeatStartDate;
    
    @Schema(
        description = "매주 반복 시 선택된 요일 (0: 일요일, 1: 월요일, ..., 6: 토요일)"
    )
    private Set<Integer> daysOfWeek;
    
    @Schema(
        description = "반복 종료일", 
        example = "2026-12-31"
    )
    private LocalDate repeatEndDate;
    
    @Schema(
        description = "태그 목록",
        type = "array"
    )
    private Set<String> tags;
}
