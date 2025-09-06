package point.ttodoApi.shared.exception.member;

import point.ttodoApi.shared.error.*;

import java.util.UUID;

public class MemberNotFoundException extends DataNotFoundException {

  public MemberNotFoundException(Long memberId) {
    super(ErrorCode.MEMBER_NOT_FOUND,
            String.format("회원(ID: %d)을 찾을 수 없습니다.", memberId));
  }

  public MemberNotFoundException(UUID memberId) {
    super(ErrorCode.MEMBER_NOT_FOUND,
            String.format("회원(ID: %s)을 찾을 수 없습니다.", memberId));
  }

  public MemberNotFoundException(String email) {
    super(ErrorCode.MEMBER_NOT_FOUND,
            String.format("회원(이메일: %s)을 찾을 수 없습니다.", email));
  }

  public MemberNotFoundException() {
    super(ErrorCode.MEMBER_NOT_FOUND);
  }
}