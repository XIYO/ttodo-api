package point.zzicback.experience.application.dto.result;

public record MemberLevelResult(
        int currentLevel,
        String levelName,
        int currentExp,
        int currentLevelMinExp,
        int expToNextLevel,
        int currentLevelProgress,
        int currentLevelTotal
) {
}
