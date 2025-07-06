package point.zzicback.common.error;

import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BIZ_001";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BIZ_001";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
