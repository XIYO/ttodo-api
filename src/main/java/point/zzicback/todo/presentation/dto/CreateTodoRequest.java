package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import point.zzicback.todo.domain.RepeatType;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Todo 생성 요청 DTO", 
    example = """
        {
          "title": "영어 공부하기",
          "description": "토익 문제집 2장 풀기",
          "statusId": 0,
          "priorityId": 1,
          "categoryId": 1,
          "dueDate": "2026-01-01T00:00:00Z",
          "repeatType": "NONE",
          "tags": "영어,학습"
        }
        """
)
public class CreateTodoRequest {
    
    @NotBlank 
    @Size(max = 255) 
    @Schema(
        description = "할일 제목", 
        example = "영어 공부하기", 
        required = true,
        minLength = 1,
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
        description = "상태 (0: 진행중, 1: 완료)", 
        example = "0", 
        defaultValue = "0", 
        allowableValues = {"0", "1"}
    )
    private Integer statusId;
    
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
    private LocalDate dueDate;

    @Schema(
        description = "마감 시간",
        example = "18:00",
        format = "time"
    )
    private LocalTime dueTime;
    
    @Schema(
        description = "반복 유형", 
        example = "NONE", 
        defaultValue = "NONE", 
        allowableValues = {"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"}
    )
    private RepeatType repeatType;
    
    @Schema(
        description = "태그 목록 (콤마로 구분)", 
        example = "영어,학습",
        type = "string"
    )
    private String tags;
}
