package point.ttodoApi.experience.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.experience.application.result.UserLevelResult;
import point.ttodoApi.experience.domain.UserExperience;
import point.ttodoApi.experience.infrastructure.UserExperienceRepository;
import point.ttodoApi.level.application.LevelService;
import point.ttodoApi.level.domain.Level;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExperienceService {
  private final UserExperienceRepository repository;
  private final LevelService levelService;

  public void addExperience(UUID userId, int amount) {
    UserExperience exp = repository.findByOwnerId(userId)
            .orElseGet(() -> repository.save(UserExperience.builder()
                    .ownerId(userId)
                    .experience(0)
                    .build()));
    exp.addExperience(amount);
  }

  public void subtractExperience(UUID userId, int amount) {
    UserExperience exp = repository.findByOwnerId(userId)
            .orElseGet(() -> repository.save(UserExperience.builder()
                    .ownerId(userId)
                    .experience(0)
                    .build()));
    exp.subtractExperience(amount);
  }

  @Transactional(readOnly = true)
  public int getExperience(UUID userId) {
    return repository.findByOwnerId(userId)
            .map(UserExperience::getExperience)
            .orElse(0);
  }

  @Transactional(readOnly = true)
  public Level getCurrentLevel(UUID userId) {
    int experience = getExperience(userId);
    Level level = levelService.getLevelByExperience(experience);
    return level != null ? level : createDefaultLevel();
  }

  @Transactional(readOnly = true)
  public UserLevelResult getUserLevel(UUID userId) {
    int experience = getExperience(userId);
    Level currentLevel = getCurrentLevel(userId);

    Level nextLevel = levelService.getNextLevel(currentLevel.getLevel());
    int experienceToNext = nextLevel != null ? nextLevel.getRequiredExp() - experience : 0;

    int currentLevelProgress = experience - currentLevel.getRequiredExp();
    int currentLevelTotal = nextLevel != null ?
            nextLevel.getRequiredExp() - currentLevel.getRequiredExp() : 0;

    return new UserLevelResult(
            currentLevel.getLevel(),
            currentLevel.getName(),
            experience,
            currentLevel.getRequiredExp(),
            experienceToNext,
            currentLevelProgress,
            currentLevelTotal
    );
  }

  private static Level createDefaultLevel() {
    return Level.builder()
            .level(1)
            .name("찍찍 초심자")
            .requiredExp(0)
            .build();
  }
}
