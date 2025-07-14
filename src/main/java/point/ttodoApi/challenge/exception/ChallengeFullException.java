package point.ttodoApi.challenge.exception;

import point.ttodoApi.common.error.BaseException;
import point.ttodoApi.common.error.ErrorCode;

public class ChallengeFullException extends BaseException {
    
    public ChallengeFullException(String challengeTitle, int maxParticipants) {
        super(ErrorCode.CHALLENGE_FULL, 
              String.format("챌린지 '%s'의 참여 인원이 가득찼습니다. (최대 %d명)", 
                          challengeTitle, maxParticipants));
    }
    
    public ChallengeFullException() {
        super(ErrorCode.CHALLENGE_FULL);
    }
}