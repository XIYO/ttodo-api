package point.ttodoApi.shared.error;

/**
 * 중복된 리소스로 인한 충돌이 발생할 때 사용하는 예외
 * 409 Conflict 상태 코드를 반환
 */
public class DuplicateResourceException extends BaseException {

  public DuplicateResourceException(ErrorCode errorCode) {
    super(errorCode);
  }

  public DuplicateResourceException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
    super(ErrorCode.DEPENDENCY_EXISTS,
            String.format("%s의 %s '%s'은(는) 이미 사용중입니다.", resourceName, fieldName, fieldValue));
  }
}