package point.ttodoApi.common.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import point.ttodoApi.common.dto.BaseSearchRequest;
import point.ttodoApi.common.metrics.SearchMetrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 검색 파라미터 로깅 및 모니터링 Aspect
 * 검색 API의 사용 패턴을 분석하고 성능을 모니터링
 */
@Slf4j
@Aspect
@Component
@Order(2) // PageableValidationAspect 다음에 실행
@RequiredArgsConstructor
public class SearchParameterLoggingAspect {
    
    private final SearchMetrics searchMetrics;
    
    /**
     * 검색 요청 로깅 및 메트릭 수집
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController) && " +
            "execution(* *..search*(..))")
    public Object logSearchRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        // 요청 파라미터 추출
        Map<String, Object> searchParams = extractSearchParameters(joinPoint);
        
        // 요청 로깅
        if (log.isDebugEnabled()) {
            log.debug("Search request: {} with params: {}", methodName, searchParams);
        }
        
        try {
            // 메서드 실행
            Object result = joinPoint.proceed();
            
            // 실행 시간 계산
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 결과 분석
            long resultCount = extractResultCount(result);
            
            // 성공 메트릭 기록
            searchMetrics.recordSearchRequest(
                methodName,
                searchParams,
                executionTime,
                resultCount,
                true
            );
            
            // 성능 로깅
            if (executionTime > 1000) {
                log.warn("Slow search detected: {} took {}ms with params: {}", 
                        methodName, executionTime, searchParams);
            } else if (log.isInfoEnabled()) {
                log.info("Search completed: {} in {}ms, returned {} results", 
                        methodName, executionTime, resultCount);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 실패 메트릭 기록
            searchMetrics.recordSearchRequest(
                methodName,
                searchParams,
                executionTime,
                0,
                false
            );
            
            log.error("Search failed: {} after {}ms with params: {}, error: {}", 
                    methodName, executionTime, searchParams, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 페이지네이션 파라미터 검증 로깅
     */
    @Before("@annotation(point.ttodoApi.common.validation.ValidPageable)")
    public void logPageableValidation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        for (Object arg : args) {
            if (arg instanceof Pageable) {
                Pageable pageable = (Pageable) arg;
                
                if (log.isDebugEnabled()) {
                    log.debug("Pageable validation: page={}, size={}, sort={}", 
                            pageable.getPageNumber(), 
                            pageable.getPageSize(),
                            pageable.getSort());
                }
                
                // 페이지 크기 메트릭
                searchMetrics.recordPageSize(pageable.getPageSize());
                
                // 정렬 필드 사용 빈도
                pageable.getSort().forEach(order -> 
                    searchMetrics.recordSortField(order.getProperty())
                );
                
                break;
            }
        }
    }
    
    /**
     * 검색 파라미터 추출
     */
    private Map<String, Object> extractSearchParameters(JoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = getParameterNames(joinPoint);
        
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            Object arg = args[i];
            String paramName = paramNames[i];
            
            if (arg instanceof BaseSearchRequest) {
                // SearchRequest 객체의 필드 추출
                BaseSearchRequest searchRequest = (BaseSearchRequest) arg;
                params.put("page", searchRequest.getPage());
                params.put("size", searchRequest.getSize());
                params.put("sort", searchRequest.getSort());
                
                // 하위 클래스의 추가 필드 추출 (리플렉션 사용 가능)
                extractAdditionalFields(searchRequest, params);
                
            } else if (arg instanceof Pageable) {
                Pageable pageable = (Pageable) arg;
                params.put("page", pageable.getPageNumber());
                params.put("size", pageable.getPageSize());
                params.put("sort", pageable.getSort().toString());
                
            } else if (isPrimitiveOrWrapper(arg)) {
                params.put(paramName, arg);
            }
        }
        
        return params;
    }
    
    /**
     * SearchRequest의 추가 필드 추출
     */
    private void extractAdditionalFields(BaseSearchRequest request, Map<String, Object> params) {
        // 실제 구현에서는 리플렉션을 사용하여 모든 필드 추출
        // 여기서는 주요 필드만 예시로 추가
        if (request.getClass().getSimpleName().contains("Todo")) {
            // TodoSearchRequest specific fields
            params.put("searchType", "todo");
        } else if (request.getClass().getSimpleName().contains("Member")) {
            // MemberSearchRequest specific fields
            params.put("searchType", "member");
        }
        // ... 다른 타입들
    }
    
    /**
     * 결과 개수 추출
     */
    private long extractResultCount(Object result) {
        if (result instanceof Page) {
            return ((Page<?>) result).getTotalElements();
        } else if (result instanceof java.util.Collection) {
            return ((java.util.Collection<?>) result).size();
        }
        return 1; // 단일 객체
    }
    
    /**
     * 파라미터 이름 추출
     */
    private String[] getParameterNames(JoinPoint joinPoint) {
        // 실제 구현에서는 MethodSignature를 사용하여 파라미터 이름 추출
        // 여기서는 간단한 예시
        return new String[]{"param1", "param2", "param3", "param4", "param5"};
    }
    
    /**
     * 기본 타입 또는 래퍼 타입 확인
     */
    private boolean isPrimitiveOrWrapper(Object obj) {
        if (obj == null) return false;
        
        Class<?> clazz = obj.getClass();
        return clazz.isPrimitive() || 
               clazz == String.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Boolean.class ||
               clazz == UUID.class ||
               Number.class.isAssignableFrom(clazz);
    }
}