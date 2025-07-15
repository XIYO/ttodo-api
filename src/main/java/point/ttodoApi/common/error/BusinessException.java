package point.ttodoApi.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외 클래스
 * 비즈니스 로직에서 발생하는 예외를 처리하기 위한 클래스
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.INVALID_ARGUMENT;
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCodeValue() {
        return errorCode.getCode();
    }
    
    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
