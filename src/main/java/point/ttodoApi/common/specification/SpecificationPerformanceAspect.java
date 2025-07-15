package point.ttodoApi.common.specification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Specification 쿼리 성능 모니터링 Aspect
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SpecificationPerformanceAspect {
    
    private final IndexAdvisor indexAdvisor;
    
    @Around("execution(* org.springframework.data.jpa.repository.JpaSpecificationExecutor+.*(org.springframework.data.jpa.domain.Specification,..))")
    public Object monitorSpecificationQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            // 쿼리 실행
            Object result = joinPoint.proceed();
            
            // 실행 시간 계산
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Specification 분석
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Specification) {
                analyzeAndRecordQuery(joinPoint, (Specification<?>) args[0], executionTime);
            }
            
            // 느린 쿼리 경고
            if (executionTime > 1000) {
                log.warn("Slow query detected: {} took {}ms", 
                        joinPoint.getSignature().toShortString(), executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Query failed after {}ms: {}", executionTime, e.getMessage());
            throw e;
        }
    }
    
    private void analyzeAndRecordQuery(ProceedingJoinPoint joinPoint, 
                                     Specification<?> spec, 
                                     long executionTime) {
        try {
            // Repository 인터페이스에서 엔티티 타입 추출
            String entityName = extractEntityName(joinPoint);
            
            // Specification에서 사용된 필드 추출 (간단한 분석)
            Set<String> usedFields = extractUsedFields(spec);
            
            // 통계 기록
            if (!usedFields.isEmpty()) {
                indexAdvisor.recordQuery(entityName, usedFields, executionTime);
                
                if (log.isDebugEnabled()) {
                    log.debug("Query on {} using fields {} took {}ms", 
                            entityName, usedFields, executionTime);
                }
            }
        } catch (Exception e) {
            log.error("Failed to analyze query: {}", e.getMessage());
        }
    }
    
    private String extractEntityName(ProceedingJoinPoint joinPoint) {
        // Repository 인터페이스 이름에서 엔티티 이름 추출
        String repositoryName = joinPoint.getTarget().getClass().getSimpleName();
        if (repositoryName.contains("Repository")) {
            return repositoryName.replace("Repository", "")
                              .replace("$Proxy", "")
                              .replaceAll("\\d+", "");
        }
        return "Unknown";
    }
    
    private Set<String> extractUsedFields(Specification<?> spec) {
        // 실제 구현에서는 Specification의 구조를 분석하여 사용된 필드를 추출
        // 여기서는 간단한 예시만 제공
        Set<String> fields = new HashSet<>();
        
        // Specification toString()이나 리플렉션을 통해 필드 추출
        String specString = spec.toString();
        
        // 일반적인 필드 패턴 매칭 (예시)
        if (specString.contains("member.id")) fields.add("member.id");
        if (specString.contains("active")) fields.add("active");
        if (specString.contains("title")) fields.add("title");
        if (specString.contains("createdAt")) fields.add("createdAt");
        if (specString.contains("complete")) fields.add("complete");
        if (specString.contains("category.id")) fields.add("category.id");
        
        return fields;
    }
}