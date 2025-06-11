package point.zzicback.challenge.application.dto.result;

import point.zzicback.challenge.domain.PeriodType;
import java.time.LocalDate;

public record ChallengeTodoDto(
        Long id,
        String challengeTitle,
        String challengeDescription,
        LocalDate startDate,
        LocalDate endDate,
        Boolean done,
        Boolean isPersisted,
        PeriodType periodType
) {
    public static ChallengeTodoDto from(point.zzicback.challenge.domain.ChallengeTodo challengeTodo) {
        // Null 체크로 NullPointerException 방지
        if (challengeTodo == null) {
            throw new IllegalArgumentException("ChallengeTodo cannot be null");
        }
        
        var participation = challengeTodo.getChallengeParticipation();
        if (participation == null) {
            throw new IllegalArgumentException("ChallengeParticipation cannot be null");
        }
        
        var challenge = participation.getChallenge();
        if (challenge == null) {
            throw new IllegalArgumentException("Challenge cannot be null");
        }
        
        var period = challengeTodo.getPeriod();
        
        return new ChallengeTodoDto(
                challengeTodo.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                period.startDate(),
                period.endDate(),
                challengeTodo.getDone(),
                challengeTodo.getId() != null,
                challenge.getPeriodType()
        );
    }
}
