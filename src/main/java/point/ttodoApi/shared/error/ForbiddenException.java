package point.ttodoApi.shared.error;

/**
 * 접근 권한이 없을 때 발생하는 예외
 */
public class ForbiddenException extends BusinessException {

  public ForbiddenException(String message) {
    super(ErrorCode.ACCESS_DENIED, message);
  }
}