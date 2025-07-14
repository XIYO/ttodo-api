package point.ttodoApi.auth.exception;

import point.ttodoApi.common.error.UnauthorizedException;
import point.ttodoApi.common.error.ErrorCode;

public class InvalidCredentialsException extends UnauthorizedException {
    
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 일치하지 않습니다.");
    }
    
    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }
}