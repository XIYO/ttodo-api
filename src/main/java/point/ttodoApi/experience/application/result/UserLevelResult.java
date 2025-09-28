package point.ttodoApi.experience.application.result;

public record UserLevelResult(
        int currentLevel,
        String levelName,
        int currentExp,
        int currentLevelMinExp,
        int expToNextLevel,
        int currentLevelProgress,
        int currentLevelTotal
) {
}
