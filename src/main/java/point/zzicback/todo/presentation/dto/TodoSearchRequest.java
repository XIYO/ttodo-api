package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Todo 검색 요청")
public record TodoSearchRequest(
        @ArraySchema(
            schema = @Schema(implementation = Integer.class, description = "할일 상태 필터 목록 (0: 진행중, 1: 완료, 2: 지연)"),
            arraySchema = @Schema(example = "[1]")
        )
        List<Integer> statusIds,
        
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
        
        @Schema(description = "검색 시작 날짜", example = "2024-01-01", type = "string", format = "date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        
        @Schema(description = "검색 종료 날짜", example = "2024-01-31", type = "string", format = "date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        
        @Schema(description = "검색 키워드 (제목, 설명, 태그에서 검색)", example = "영어")
        String keyword,
        
        @ArraySchema(
            schema = @Schema(implementation = Integer.class, description = "숨길 상태 ID 목록"),
            arraySchema = @Schema(example = "[2]")
        )
        List<Integer> hideStatusIds,
        
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