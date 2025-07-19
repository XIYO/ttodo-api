package point.ttodoApi.common.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExceptionLoggingAspect {
    
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || " +
              "@within(org.springframework.stereotype.Controller)")
    public void controllerMethods() {}
    
    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceMethods() {}
    
    @Pointcut("@within(org.springframework.stereotype.Repository)")
    public void repositoryMethods() {}
    
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logControllerException(JoinPoint joinPoint, Throwable exception) {
        if (exception instanceof BaseException || exception instanceof BusinessException) {
            logBusinessException(joinPoint, exception);
        } else {
            logUnexpectedException(joinPoint, exception);
        }
    }
    
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Throwable exception) {
        if (exception instanceof BaseException || exception instanceof BusinessException) {
            // Business exceptions in service layer are expected flow
            log.debug("Business exception in service layer: {} - {}", 
                     exception.getClass().getSimpleName(), exception.getMessage());
        } else {
            logUnexpectedException(joinPoint, exception);
        }
    }
    
    @AfterThrowing(pointcut = "repositoryMethods()", throwing = "exception")
    public void logRepositoryException(JoinPoint joinPoint, Throwable exception) {
        log.error("Repository exception in {}.{}", 
                  joinPoint.getSignature().getDeclaringTypeName(),
                  joinPoint.getSignature().getName(),
                  exception);
    }
    
    private void logBusinessException(JoinPoint joinPoint, Throwable exception) {
        Map<String, Object> errorDetails = buildErrorDetails(joinPoint, exception);
        
        if (exception instanceof BaseException) {
            BaseException baseEx = (BaseException) exception;
            errorDetails.put("errorCode", baseEx.getErrorCode());
            errorDetails.put("httpStatus", baseEx.getHttpStatus().value());
        } else if (exception instanceof BusinessException) {
            BusinessException bizEx = (BusinessException) exception;
            errorDetails.put("errorCode", bizEx.getErrorCodeValue());
            errorDetails.put("httpStatus", bizEx.getHttpStatus().value());
        }
        
        log.warn("Business exception: {}", errorDetails);
    }
    
    private void logUnexpectedException(JoinPoint joinPoint, Throwable exception) {
        Map<String, Object> errorDetails = buildErrorDetails(joinPoint, exception);
        errorDetails.put("stackTrace", getStackTraceHead(exception, 5));
        
        log.error("Unexpected exception: {}", errorDetails, exception);
    }
    
    private Map<String, Object> buildErrorDetails(JoinPoint joinPoint, Throwable exception) {
        Map<String, Object> details = new HashMap<>();
        
        details.put("timestamp", LocalDateTime.now());
        details.put("exceptionType", exception.getClass().getName());
        details.put("message", exception.getMessage());
        details.put("method", joinPoint.getSignature().toShortString());
        
        // Add request details if available
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            details.put("requestUri", request.getRequestURI());
            details.put("httpMethod", request.getMethod());
            details.put("remoteAddress", request.getRemoteAddr());
            
            // Add user info if available
            if (request.getUserPrincipal() != null) {
                details.put("user", request.getUserPrincipal().getName());
            }
        }
        
        // Add method arguments (be careful with sensitive data)
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            details.put("methodArgs", sanitizeArguments(args));
        }
        
        return details;
    }
    
    private String[] sanitizeArguments(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    }
                    String className = arg.getClass().getSimpleName();
                    // Avoid logging sensitive data
                    if (className.toLowerCase().contains("password") || 
                        className.toLowerCase().contains("token") ||
                        className.toLowerCase().contains("credential")) {
                        return className + "[REDACTED]";
                    }
                    return className + "[" + arg.toString().substring(0, Math.min(arg.toString().length(), 100)) + "]";
                })
                .toArray(String[]::new);
    }
    
    private String[] getStackTraceHead(Throwable exception, int lines) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        int limit = Math.min(lines, stackTrace.length);
        String[] head = new String[limit];
        
        for (int i = 0; i < limit; i++) {
            head[i] = stackTrace[i].toString();
        }
        
        return head;
    }
}