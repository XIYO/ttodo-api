package point.ttodoApi.common.specification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 동적 쿼리 시스템 설정
 */
@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@RequiredArgsConstructor
public class SpecificationConfig {
    
    private final IndexAdvisor indexAdvisor;
    
    /**
     * 주기적으로 인덱스 추천 로그 출력 (개발 환경에서만)
     */
    @Scheduled(fixedDelay = 3600000) // 1시간마다
    @ConditionalOnProperty(name = "app.specification.index-advisor.enabled", havingValue = "true")
    public void logIndexRecommendations() {
        var recommendations = indexAdvisor.getIndexRecommendations();
        
        if (!recommendations.isEmpty()) {
            log.info("=== Index Recommendations ===");
            recommendations.forEach(rec -> 
                log.info("{}", rec)
            );
            
            log.info("=== SQL Statements ===");
            indexAdvisor.generateIndexSql().forEach(log::info);
        }
    }
    
    /**
     * 매일 자정에 통계 리셋 (선택적)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @ConditionalOnProperty(name = "app.specification.index-advisor.daily-reset", havingValue = "true")
    public void resetDailyStatistics() {
        log.info("Resetting query statistics for new day");
        indexAdvisor.resetStatistics();
    }
}