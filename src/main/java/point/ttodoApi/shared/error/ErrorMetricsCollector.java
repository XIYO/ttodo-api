package point.ttodoApi.shared.error;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ErrorMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();
    
    public ErrorMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordError(String errorCode, int httpStatus, String exceptionType) {
        try {
            String counterKey = String.format("error_%s_%d", errorCode, httpStatus);
            
            Counter counter = errorCounters.computeIfAbsent(counterKey, k -> 
                Counter.builder("application.errors")
                    .description("Application error count")
                    .tags(Tags.of(
                        "error_code", errorCode,
                        "http_status", String.valueOf(httpStatus),
                        "exception_type", exceptionType
                    ))
                    .register(meterRegistry)
            );
            
            counter.increment();
            
        } catch (Exception e) {
            log.warn("Failed to record error metric", e);
        }
    }
    
    public void recordValidationError(String field) {
        try {
            Counter counter = Counter.builder("application.validation.errors")
                .description("Validation error count")
                .tag("field", field)
                .register(meterRegistry);
            
            counter.increment();
            
        } catch (Exception e) {
            log.warn("Failed to record validation error metric", e);
        }
    }
    
    public void recordAuthenticationFailure(String reason) {
        try {
            Counter counter = Counter.builder("application.auth.failures")
                .description("Authentication failure count")
                .tag("reason", reason)
                .register(meterRegistry);
            
            counter.increment();
            
        } catch (Exception e) {
            log.warn("Failed to record authentication failure metric", e);
        }
    }
}