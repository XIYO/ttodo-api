package point.ttodoApi.auth.exception;

import point.ttodoApi.common.error.*;

public class TokenExpiredException extends UnauthorizedException {
    
    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED, "토큰이 만료되었습니다.");
    }
    
    public TokenExpiredException(String message) {
        super(ErrorCode.TOKEN_EXPIRED, message);
    }
}