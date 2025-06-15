package point.zzicback.challenge.application.dto.result;

import java.util.List;

public record ChallengeParticipantsResult(
        Long challengeId,
        String title,
        List<ParticipantResult> participants
) {
}
