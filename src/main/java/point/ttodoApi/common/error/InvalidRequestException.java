package point.ttodoApi.common.error;

/**
 * 잘못된 요청 데이터로 인한 예외
 * 400 Bad Request 상태 코드를 반환
 */
public class InvalidRequestException extends BaseException {
    
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public InvalidRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public InvalidRequestException(String message) {
        super(ErrorCode.INVALID_ARGUMENT, message);
    }
}