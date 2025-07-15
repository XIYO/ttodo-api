package point.ttodoApi.common.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 동적 쿼리 사용 패턴을 분석하여 인덱스 생성을 제안하는 유틸리티
 */
@Slf4j
@Component
public class IndexAdvisor {
    
    // 쿼리 사용 통계를 저장
    private final Map<String, QueryStatistics> queryStats = new HashMap<>();
    
    /**
     * 쿼리 실행 기록
     */
    public void recordQuery(String entityName, Set<String> usedFields, long executionTime) {
        String key = entityName + ":" + String.join(",", new TreeSet<>(usedFields));
        
        queryStats.compute(key, (k, stats) -> {
            if (stats == null) {
                stats = new QueryStatistics(entityName, usedFields);
            }
            stats.recordExecution(executionTime);
            return stats;
        });
    }
    
    /**
     * 인덱스 추천 생성
     */
    public List<IndexRecommendation> getIndexRecommendations() {
        List<IndexRecommendation> recommendations = new ArrayList<>();
        
        // 실행 빈도와 평균 실행 시간을 기준으로 인덱스 추천
        queryStats.entrySet().stream()
                .filter(entry -> entry.getValue().shouldCreateIndex())
                .forEach(entry -> {
                    QueryStatistics stats = entry.getValue();
                    recommendations.add(new IndexRecommendation(
                            stats.entityName,
                            new ArrayList<>(stats.fields),
                            stats.executionCount,
                            stats.getAverageExecutionTime(),
                            generateIndexName(stats.entityName, stats.fields)
                    ));
                });
        
        // 중요도 순으로 정렬
        recommendations.sort((a, b) -> {
            // 실행 빈도와 평균 실행 시간을 고려한 점수 계산
            double scoreA = a.executionCount * Math.log(a.averageExecutionTime + 1);
            double scoreB = b.executionCount * Math.log(b.averageExecutionTime + 1);
            return Double.compare(scoreB, scoreA);
        });
        
        return recommendations;
    }
    
    /**
     * 인덱스 추천 SQL 생성
     */
    public List<String> generateIndexSql() {
        List<String> sqlStatements = new ArrayList<>();
        
        getIndexRecommendations().forEach(rec -> {
            String tableName = camelToSnake(rec.entityName);
            String columnList = rec.fields.stream()
                    .map(this::camelToSnake)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            
            String sql = String.format(
                    "CREATE INDEX %s ON %s (%s);",
                    rec.indexName,
                    tableName,
                    columnList
            );
            
            sqlStatements.add(sql);
            
            // 코멘트 추가
            String comment = String.format(
                    "-- Recommended: %d executions, avg %.2fms",
                    rec.executionCount,
                    rec.averageExecutionTime
            );
            sqlStatements.add(comment);
        });
        
        return sqlStatements;
    }
    
    /**
     * 통계 리셋
     */
    public void resetStatistics() {
        queryStats.clear();
        log.info("Query statistics reset");
    }
    
    private String generateIndexName(String entityName, Set<String> fields) {
        String prefix = "idx_" + camelToSnake(entityName);
        String fieldsPart = fields.stream()
                .map(this::camelToSnake)
                .limit(3) // 인덱스 이름이 너무 길어지지 않도록
                .reduce((a, b) -> a + "_" + b)
                .orElse("");
        
        return prefix + "_" + fieldsPart;
    }
    
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    /**
     * 쿼리 통계 클래스
     */
    private static class QueryStatistics {
        private final String entityName;
        private final Set<String> fields;
        private long executionCount = 0;
        private long totalExecutionTime = 0;
        private long maxExecutionTime = 0;
        
        public QueryStatistics(String entityName, Set<String> fields) {
            this.entityName = entityName;
            this.fields = new HashSet<>(fields);
        }
        
        public void recordExecution(long executionTime) {
            executionCount++;
            totalExecutionTime += executionTime;
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
        }
        
        public double getAverageExecutionTime() {
            return executionCount > 0 ? (double) totalExecutionTime / executionCount : 0;
        }
        
        public boolean shouldCreateIndex() {
            // 인덱스 생성 기준:
            // 1. 10회 이상 실행
            // 2. 평균 실행 시간 50ms 이상
            // 3. 또는 100회 이상 실행
            return (executionCount >= 10 && getAverageExecutionTime() >= 50) ||
                   executionCount >= 100;
        }
    }
    
    /**
     * 인덱스 추천 정보
     */
    public static class IndexRecommendation {
        public final String entityName;
        public final List<String> fields;
        public final long executionCount;
        public final double averageExecutionTime;
        public final String indexName;
        
        public IndexRecommendation(String entityName, List<String> fields, 
                                 long executionCount, double averageExecutionTime, 
                                 String indexName) {
            this.entityName = entityName;
            this.fields = fields;
            this.executionCount = executionCount;
            this.averageExecutionTime = averageExecutionTime;
            this.indexName = indexName;
        }
        
        @Override
        public String toString() {
            return String.format("Index on %s(%s) - %d queries, avg %.2fms",
                    entityName, String.join(", ", fields), 
                    executionCount, averageExecutionTime);
        }
    }
}