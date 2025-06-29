package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Todo 수정 요청 DTO",
    example = """
        {
          "title": "영어 공부하기 (수정)",
          "description": "토익 문제집 3장 풀기",
          "statusId": 1,
          "priorityId": 2,
          "categoryId": 1,
          "dueDate": "2026-01-02T00:00:00Z",
          "repeatType": "DAILY",
          "tags": ["영어", "학습", "토익"]
        }
        """
)
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
        description = "상태 (0: 진행중, 1: 완료)", 
        example = "0", 
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
        description = "매주 반복 시 선택된 요일 (0: 일요일, 1: 월요일, ..., 6: 토요일)",
        example = "[1, 3, 5]"
    )
    private Set<Integer> daysOfWeek;
    
    @Schema(
        description = "반복 종료일", 
        example = "2026-12-31"
    )
    private LocalDate repeatEndDate;
    
    @Schema(
        description = "태그 목록. 같은 이름으로 여러 번 전달합니다.",
        example = "tags=영어&tags=학습&tags=토익",
        type = "array"
    )
    private Set<String> tags;
}
