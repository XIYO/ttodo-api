package point.zzicback.common.error;

import org.springframework.http.HttpStatus;

/**
 * 접근 권한이 없을 때 발생하는 예외
 */
public class ForbiddenException extends BusinessException {
    
    public ForbiddenException(String message) {
        super("AUTH_002", message, HttpStatus.FORBIDDEN);
    }
}