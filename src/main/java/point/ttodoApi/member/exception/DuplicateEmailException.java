package point.ttodoApi.member.exception;

import point.ttodoApi.common.error.DuplicateResourceException;
import point.ttodoApi.common.error.ErrorCode;

public class DuplicateEmailException extends DuplicateResourceException {
    
    public DuplicateEmailException(String email) {
        super(ErrorCode.DUPLICATE_EMAIL, 
              String.format("이메일 '%s'은(는) 이미 사용중입니다.", email));
    }
    
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}