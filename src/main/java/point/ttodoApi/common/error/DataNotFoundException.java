package point.ttodoApi.common.error;

/**
 * 데이터를 찾을 수 없을 때 발생하는 예외
 * 404 Not Found 상태 코드를 반환
 */
public class DataNotFoundException extends BaseException {
    
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public DataNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public DataNotFoundException(String entityName, Long id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              String.format("%s(ID: %d)를 찾을 수 없습니다.", entityName, id));
    }
    
    public DataNotFoundException(String entityName, String identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              String.format("%s(%s)를 찾을 수 없습니다.", entityName, identifier));
    }
}