package point.ttodoApi.member.exception;

import point.ttodoApi.shared.error.*;

public class InvalidPasswordException extends InvalidRequestException {
    
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD, "비밀번호가 일치하지 않습니다.");
    }
    
    public InvalidPasswordException(String message) {
        super(ErrorCode.INVALID_PASSWORD, message);
    }
}