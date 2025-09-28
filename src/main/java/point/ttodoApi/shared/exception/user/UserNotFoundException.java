package point.ttodoApi.shared.exception.user;

import point.ttodoApi.shared.error.*;

import java.util.UUID;

public class UserNotFoundException extends DataNotFoundException {

  public UserNotFoundException(Long userId) {
    super(ErrorCode.MEMBER_NOT_FOUND,
            String.format("회원(ID: %d)을 찾을 수 없습니다.", userId));
  }

  public UserNotFoundException(UUID userId) {
    super(ErrorCode.MEMBER_NOT_FOUND,
            String.format("회원(ID: %s)을 찾을 수 없습니다.", userId));
  }

  public UserNotFoundException(String email) {
    super(ErrorCode.MEMBER_NOT_FOUND,
            String.format("회원(이메일: %s)을 찾을 수 없습니다.", email));
  }

  public UserNotFoundException() {
    super(ErrorCode.MEMBER_NOT_FOUND);
  }
}