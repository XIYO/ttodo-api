package point.ttodoApi.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        long startTime = System.currentTimeMillis();
        
        if (log.isDebugEnabled()) {
            log.debug("Enter: {}.{}() with args = {}", className, methodName, Arrays.toString(joinPoint.getArgs()));
        }
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("⏱️ Performance: {}.{}() executed in {} ms", className, methodName, executionTime);
            
            if (log.isDebugEnabled()) {
                log.debug("Exit: {}.{}() with result = {}", className, methodName, result);
            }
            
            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("❌ Exception in {}.{}() after {} ms with cause = {}", className, methodName, executionTime, e.getCause() != null ? e.getCause() : "NULL", e);
            throw e;
        }
    }
}
