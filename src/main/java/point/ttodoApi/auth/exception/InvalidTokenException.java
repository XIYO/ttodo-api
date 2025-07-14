package point.ttodoApi.auth.exception;

import point.ttodoApi.common.error.UnauthorizedException;
import point.ttodoApi.common.error.ErrorCode;

public class InvalidTokenException extends UnauthorizedException {
    
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
    }
    
    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
    
    public InvalidTokenException(Throwable cause) {
        super(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
    }
}