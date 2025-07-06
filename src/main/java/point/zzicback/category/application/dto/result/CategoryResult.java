package point.zzicback.category.application.dto.result;

import java.time.Instant;

/**
 * 카테고리 조회 결과 DTO
 * Application Layer에서 사용하는 DTO로, Presentation Layer의 Response와 분리
 */
public record CategoryResult(
        Long id,
        String name,
        String colorHex,
        String description,
        Integer displayOrder,
        Instant createdAt,
        Instant updatedAt
) {
}