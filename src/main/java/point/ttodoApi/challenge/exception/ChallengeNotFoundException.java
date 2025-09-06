package point.ttodoApi.challenge.exception;

import point.ttodoApi.shared.error.*;

public class ChallengeNotFoundException extends DataNotFoundException {

  public ChallengeNotFoundException(Long challengeId) {
    super(ErrorCode.CHALLENGE_NOT_FOUND,
            String.format("챌린지(ID: %d)를 찾을 수 없습니다.", challengeId));
  }

  public ChallengeNotFoundException(String inviteCode) {
    super(ErrorCode.CHALLENGE_NOT_FOUND,
            String.format("초대 코드 '%s'에 해당하는 챌린지를 찾을 수 없습니다.", inviteCode));
  }

  public ChallengeNotFoundException() {
    super(ErrorCode.CHALLENGE_NOT_FOUND);
  }
}