package point.zzicback.common.error;

import org.springframework.http.HttpStatus;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class NotFoundException extends BusinessException {
    
    public NotFoundException(String message) {
        super("RESOURCE_001", message, HttpStatus.NOT_FOUND);
    }
    
    public NotFoundException(String resourceName, Long id) {
        super("RESOURCE_001", String.format("%s(id=%d)를 찾을 수 없습니다", resourceName, id), HttpStatus.NOT_FOUND);
    }
}