package point.ttodoApi.shared.error;

/**
 * 권한 부족으로 접근이 거부될 때 발생하는 예외
 * 403 Forbidden 상태 코드를 반환
 */
public class AccessDeniedException extends BaseException {

  public AccessDeniedException(ErrorCode errorCode) {
    super(errorCode);
  }

  public AccessDeniedException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public AccessDeniedException(String message) {
    super(ErrorCode.ACCESS_DENIED, message);
  }

  public AccessDeniedException() {
    super(ErrorCode.ACCESS_DENIED);
  }
}