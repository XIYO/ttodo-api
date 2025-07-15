package point.ttodoApi.common.error;

import org.springframework.http.HttpStatus;

/**
 * 리소스 충돌이 발생했을 때 발생하는 예외
 */
public class ConflictException extends BusinessException {
    
    public ConflictException(String message) {
        super(ErrorCode.DEPENDENCY_EXISTS, message);
    }
}