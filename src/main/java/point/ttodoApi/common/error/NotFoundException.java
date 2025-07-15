package point.ttodoApi.common.error;

import org.springframework.http.HttpStatus;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class NotFoundException extends BusinessException {
    
    public NotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
    
    public NotFoundException(String resourceName, Long id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, String.format("%s(id=%d)를 찾을 수 없습니다", resourceName, id));
    }
}