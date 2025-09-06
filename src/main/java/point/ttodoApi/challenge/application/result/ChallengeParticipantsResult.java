package point.ttodoApi.challenge.application.result;

import java.util.List;

public record ChallengeParticipantsResult(
        Long challengeId,
        String title,
        List<ParticipantResult> participants
) {
}
