package point.ttodoApi.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.common.metrics.SearchMetrics;
import point.ttodoApi.common.specification.IndexAdvisor;

import java.util.*;

/**
 * 시스템 메트릭 조회 API (관리자 전용)
 */
@Tag(name = "Metrics", description = "시스템 메트릭 및 통계 조회 API")
@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MetricsController {
    
    private final SearchMetrics searchMetrics;
    private final IndexAdvisor indexAdvisor;
    
    @Operation(summary = "검색 통계 조회", description = "검색 API 사용 통계를 조회합니다")
    @GetMapping("/search")
    public ResponseEntity<SearchMetrics.SearchStatistics> getSearchStatistics() {
        return ResponseEntity.ok(searchMetrics.getCurrentStatistics());
    }
    
    @Operation(summary = "인덱스 추천 조회", description = "쿼리 패턴 분석을 통한 인덱스 추천을 조회합니다")
    @GetMapping("/index-recommendations")
    public ResponseEntity<Map<String, Object>> getIndexRecommendations() {
        List<IndexAdvisor.IndexRecommendation> recommendations = indexAdvisor.getIndexRecommendations();
        List<String> sqlStatements = indexAdvisor.generateIndexSql();
        
        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("sqlStatements", sqlStatements);
        response.put("totalRecommendations", recommendations.size());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "검색 통계 리셋", description = "검색 통계를 초기화합니다")
    @GetMapping("/search/reset")
    public ResponseEntity<Map<String, String>> resetSearchStatistics() {
        searchMetrics.resetDailyStats();
        indexAdvisor.resetStatistics();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Search statistics have been reset");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}