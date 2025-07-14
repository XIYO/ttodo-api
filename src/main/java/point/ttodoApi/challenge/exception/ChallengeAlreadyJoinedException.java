package point.ttodoApi.challenge.exception;

import point.ttodoApi.common.error.BaseException;
import point.ttodoApi.common.error.ErrorCode;

public class ChallengeAlreadyJoinedException extends BaseException {
    
    public ChallengeAlreadyJoinedException(String challengeTitle) {
        super(ErrorCode.CHALLENGE_ALREADY_JOINED, 
              String.format("챌린지 '%s'에 이미 참여중입니다.", challengeTitle));
    }
    
    public ChallengeAlreadyJoinedException() {
        super(ErrorCode.CHALLENGE_ALREADY_JOINED);
    }
}