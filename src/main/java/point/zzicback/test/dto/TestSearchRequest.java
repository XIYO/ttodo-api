package point.zzicback.test.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.List;

@Schema(description = "Todo 검색 요청")
public record TestSearchRequest(
        @Schema(description = "할일 상태 필터 목록 (0: 진행중, 1: 완료, 2: 지연)", example = "[0,1]")
        List<Integer> statusIds,
        
        @Schema(description = "카테고리 ID 필터 목록", example = "[1,2]")
        List<Long> categoryIds,
        
        @Schema(description = "우선순위 필터 목록 (0: 낮음, 1: 보통, 2: 높음)", example = "[0,1,2]")
        List<Integer> priorityIds,
        
        @Schema(description = "태그 필터", example = "[\"학습\",\"운동\"]")
        List<String> tags,
        
        @Schema(description = "검색 시작 시각", example = "2025-06-01T00:00:00Z", type = "string", format = "date-time")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant startDate,
        
        @Schema(description = "검색 종료 시각", example = "2025-12-31T23:59:59Z", type = "string", format = "date-time")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant endDate,
        
        @Schema(description = "검색 키워드 (제목, 설명, 태그에서 검색)", example = "영어")
        String keyword,
        
        @Schema(description = "숨길 상태 ID 목록", example = "[1,2]")
        List<Integer> hideStatusIds,
        
        @Schema(description = "페이지 번호", example = "0")
        Integer page,
        
        @Schema(description = "페이지 크기", example = "10")
        Integer size
) {
    public TestSearchRequest {
        page = page != null ? page : 0;
        size = size != null ? size : 10;
    }
}