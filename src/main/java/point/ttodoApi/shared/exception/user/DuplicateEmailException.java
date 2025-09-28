package point.ttodoApi.shared.exception.user;

import point.ttodoApi.shared.error.*;

public class DuplicateEmailException extends DuplicateResourceException {

  public DuplicateEmailException(String email) {
    super(ErrorCode.DUPLICATE_EMAIL,
            String.format("이메일 '%s'은(는) 이미 사용중입니다.", email));
  }

  public DuplicateEmailException() {
    super(ErrorCode.DUPLICATE_EMAIL);
  }
}