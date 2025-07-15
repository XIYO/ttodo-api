package point.ttodoApi.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 검색 API 메트릭 수집 및 분석
 */
@Slf4j
@Component
public class SearchMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // 검색 요청 카운터
    private final Counter searchRequestCounter;
    private final Counter searchSuccessCounter;
    private final Counter searchFailureCounter;
    
    // 검색 시간 타이머
    private final Timer searchTimer;
    
    // 페이지 크기 분포
    private final Map<Integer, AtomicLong> pageSizeDistribution = new ConcurrentHashMap<>();
    
    // 정렬 필드 사용 빈도
    private final Map<String, AtomicLong> sortFieldUsage = new ConcurrentHashMap<>();
    
    // 검색 키워드 분석
    private final Map<String, AtomicLong> searchKeywords = new ConcurrentHashMap<>();
    
    // 시간대별 검색 통계
    private final Map<Integer, AtomicLong> hourlySearchCount = new ConcurrentHashMap<>();
    
    public SearchMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 메트릭 초기화
        this.searchRequestCounter = Counter.builder("search.requests.total")
                .description("Total number of search requests")
                .register(meterRegistry);
                
        this.searchSuccessCounter = Counter.builder("search.requests.success")
                .description("Number of successful search requests")
                .register(meterRegistry);
                
        this.searchFailureCounter = Counter.builder("search.requests.failure")
                .description("Number of failed search requests")
                .register(meterRegistry);
                
        this.searchTimer = Timer.builder("search.duration")
                .description("Search request duration")
                .register(meterRegistry);
    }
    
    /**
     * 검색 요청 기록
     */
    public void recordSearchRequest(String method, Map<String, Object> params, 
                                  long executionTime, long resultCount, boolean success) {
        // 기본 메트릭
        searchRequestCounter.increment();
        searchTimer.record(executionTime, TimeUnit.MILLISECONDS);
        
        if (success) {
            searchSuccessCounter.increment();
            
            // 결과 개수 메트릭
            meterRegistry.gauge("search.results.count", resultCount);
            
            // 검색 유형별 메트릭
            meterRegistry.counter("search.type", "method", method).increment();
            
        } else {
            searchFailureCounter.increment();
        }
        
        // 시간대별 통계
        int currentHour = java.time.LocalDateTime.now().getHour();
        hourlySearchCount.computeIfAbsent(currentHour, k -> new AtomicLong(0)).incrementAndGet();
        
        // 파라미터 분석
        analyzeSearchParameters(params);
    }
    
    /**
     * 페이지 크기 기록
     */
    public void recordPageSize(int pageSize) {
        pageSizeDistribution.computeIfAbsent(pageSize, k -> new AtomicLong(0)).incrementAndGet();
        
        // Gauge로 현재 페이지 크기 노출
        meterRegistry.gauge("search.page.size.current", pageSize);
    }
    
    /**
     * 정렬 필드 사용 기록
     */
    public void recordSortField(String field) {
        sortFieldUsage.computeIfAbsent(field, k -> new AtomicLong(0)).incrementAndGet();
        
        // 정렬 필드별 카운터
        meterRegistry.counter("search.sort.field", "field", field).increment();
    }
    
    /**
     * 검색 파라미터 분석
     */
    private void analyzeSearchParameters(Map<String, Object> params) {
        // 키워드 추출 및 기록
        Object keyword = params.get("keyword");
        if (keyword instanceof String && !((String) keyword).isEmpty()) {
            String normalizedKeyword = normalizeKeyword((String) keyword);
            searchKeywords.computeIfAbsent(normalizedKeyword, k -> new AtomicLong(0)).incrementAndGet();
        }
        
        // 필터 사용 통계
        params.forEach((key, value) -> {
            if (value != null && !key.equals("page") && !key.equals("size")) {
                meterRegistry.counter("search.filter.usage", "filter", key).increment();
            }
        });
    }
    
    /**
     * 키워드 정규화
     */
    private String normalizeKeyword(String keyword) {
        return keyword.toLowerCase().trim().replaceAll("\\s+", " ");
    }
    
    /**
     * 검색 통계 리포트 생성 (매시간 실행)
     */
    @Scheduled(fixedDelay = 3600000) // 1시간마다
    public void generateSearchReport() {
        log.info("=== Search Metrics Report ===");
        
        // 가장 많이 사용된 페이지 크기
        pageSizeDistribution.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                .limit(5)
                .forEach(entry -> 
                    log.info("Page size {}: {} times", entry.getKey(), entry.getValue().get())
                );
        
        // 가장 많이 사용된 정렬 필드
        log.info("Top sort fields:");
        sortFieldUsage.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                .limit(10)
                .forEach(entry -> 
                    log.info("  {}: {} times", entry.getKey(), entry.getValue().get())
                );
        
        // 인기 검색어
        if (!searchKeywords.isEmpty()) {
            log.info("Popular search keywords:");
            searchKeywords.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                    .limit(20)
                    .forEach(entry -> 
                        log.info("  '{}': {} times", entry.getKey(), entry.getValue().get())
                    );
        }
        
        // 시간대별 검색 패턴
        log.info("Hourly search pattern:");
        hourlySearchCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> 
                    log.info("  Hour {}: {} searches", entry.getKey(), entry.getValue().get())
                );
    }
    
    /**
     * 일일 통계 리셋 (매일 자정)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyStats() {
        log.info("Resetting daily search statistics");
        searchKeywords.clear();
        hourlySearchCount.clear();
    }
    
    /**
     * 현재 검색 통계 조회
     */
    public SearchStatistics getCurrentStatistics() {
        return SearchStatistics.builder()
                .totalRequests(searchRequestCounter.count())
                .successfulRequests(searchSuccessCounter.count())
                .failedRequests(searchFailureCounter.count())
                .averageResponseTime(searchTimer.mean(TimeUnit.MILLISECONDS))
                .popularPageSizes(getTopEntries(pageSizeDistribution, 5))
                .popularSortFields(getTopEntries(sortFieldUsage, 10))
                .popularKeywords(getTopEntries(searchKeywords, 20))
                .build();
    }
    
    /**
     * 상위 N개 항목 추출
     */
    private Map<String, Long> getTopEntries(Map<?, AtomicLong> map, int limit) {
        Map<String, Long> result = new ConcurrentHashMap<>();
        
        map.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                .limit(limit)
                .forEach(entry -> 
                    result.put(entry.getKey().toString(), entry.getValue().get())
                );
        
        return result;
    }
    
    /**
     * 검색 통계 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class SearchStatistics {
        private final double totalRequests;
        private final double successfulRequests;
        private final double failedRequests;
        private final double averageResponseTime;
        private final Map<String, Long> popularPageSizes;
        private final Map<String, Long> popularSortFields;
        private final Map<String, Long> popularKeywords;
    }
}