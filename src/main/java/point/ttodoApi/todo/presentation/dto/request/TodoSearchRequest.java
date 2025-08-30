package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Todo 검색 요청")
public record TodoSearchRequest(
        @ArraySchema(
            schema = @Schema(implementation = LocalDate.class, description = "날짜 필터 (1개: 단일 날짜, 2개: 범위, 3개+: 최소~최대 범위)"),
            arraySchema = @Schema(example = "[\"2025-08-30\"] 또는 [\"2025-08-01\", \"2025-08-31\"]")
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        List<LocalDate> dates,
        
        @Schema(description = "완료 상태 필터 (null: 전체, true: 완료만, false: 미완료만)", example = "false")
        Boolean complete,
        
        @ArraySchema(
            schema = @Schema(implementation = Long.class, description = "카테고리 ID 필터 목록"),
            arraySchema = @Schema(example = "[1, 2, 3]")
        )
        List<Long> categoryIds,
        
        @ArraySchema(
            schema = @Schema(implementation = Integer.class, description = "우선순위 필터 목록 (0: 낮음, 1: 보통, 2: 높음)"),
            arraySchema = @Schema(example = "[1, 2]")
        )
        List<Integer> priorityIds,
        
        @ArraySchema(
            schema = @Schema(implementation = String.class, description = "태그 필터"),
            arraySchema = @Schema(example = "[\"영어\", \"학습\"]")
        )
        List<String> tags,
        
        @Schema(description = "검색 키워드 (제목, 설명, 태그에서 검색)", example = "영어")
        String keyword,
        
        @Schema(description = "페이지 번호", example = "0", defaultValue = "0")
        Integer page,
        
        @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
        Integer size
) {
    public TodoSearchRequest {
        page = page != null ? page : 0;
        size = size != null ? size : 10;
    }
}