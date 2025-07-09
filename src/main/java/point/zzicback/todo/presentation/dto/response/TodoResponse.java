package point.zzicback.todo.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Todo 응답 DTO")
public record TodoResponse(
        @Schema(description = "Todo ID (가상 투두의 경우 '원본ID:반복순서' 형식)", example = "1 또는 11:3")
        String id, 
        
        @Schema(description = "할일 제목", example = "영어 공부하기")
        String title, 
        
        @Schema(description = "할일 설명", example = "토익 문제집 2장 풀기")
        String description, 
        
        @Schema(description = "완료 여부 (true: 완료, false: 진행중)", example = "false")
        Boolean complete,
        
        @Schema(description = "상단 고정 여부", example = "false")
        Boolean isPinned,
        
        @Schema(description = "정렬 순서 (0이 가장 먼저)", example = "0")
        Integer displayOrder,
        
        @Schema(description = "우선순위 (0: 낮음, 1: 보통, 2: 높음)", example = "1")
        Integer priorityId,
        
        @Schema(description = "우선순위명", example = "보통")
        String priorityName,
        
        @Schema(description = "카테고리 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID categoryId,
        
        @Schema(description = "카테고리명", example = "학습")
        String categoryName,
        
        @Schema(description = "마감 날짜", example = "2026-12-31")
        LocalDate date,

        @Schema(description = "마감 시간", example = "18:00")
        LocalTime time,
        
        @Schema(description = "반복 유형 (0: 반복 안함, 1: 데일리, 2: 위클리, 3: 먼슬리, 4: 이얼리)", example = "0")
        Integer repeatType,
        
        @Schema(description = "반복 간격 (일 단위)", example = "1")
        Integer repeatInterval,
        
        @Schema(description = "매주 반복 시 선택된 요일 (0: 일요일, 1: 월요일, ..., 6: 토요일)", example = "[1, 3, 5]")
        Set<Integer> daysOfWeek,
        
        @Schema(description = "반복 종료일", example = "2026-12-31")
        LocalDate repeatEndDate,
        
        @Schema(description = "태그 목록", example = "[\"영어\", \"학습\"]")
        Set<String> tags
) {
}
