package point.zzicback.challenge.application.dto.result;

import java.util.List;

public record ChallengeParticipantsDto(
        Long challengeId,
        String title,
        List<ParticipantDto> participants
) {
}
