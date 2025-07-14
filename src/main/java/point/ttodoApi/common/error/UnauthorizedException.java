package point.ttodoApi.common.error;

/**
 * 인증 실패 시 발생하는 예외
 * 401 Unauthorized 상태 코드를 반환
 */
public class UnauthorizedException extends BaseException {
    
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public UnauthorizedException(String message) {
        super(ErrorCode.AUTHENTICATION_FAILED, message);
    }
    
    public UnauthorizedException() {
        super(ErrorCode.AUTHENTICATION_FAILED);
    }
}