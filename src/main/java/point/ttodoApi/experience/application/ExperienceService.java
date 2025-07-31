package point.ttodoApi.experience.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.experience.application.dto.result.MemberLevelResult;
import point.ttodoApi.experience.domain.MemberExperience;
import point.ttodoApi.experience.infrastructure.MemberExperienceRepository;
import point.ttodoApi.level.application.LevelService;
import point.ttodoApi.level.domain.Level;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExperienceService {
    private final MemberExperienceRepository repository;
    private final LevelService levelService;

    public void addExperience(UUID memberId, int amount) {
        MemberExperience exp = repository.findByOwnerId(memberId)
                .orElseGet(() -> repository.save(MemberExperience.builder()
                        .ownerId(memberId)
                        .experience(0)
                        .build()));
        exp.addExperience(amount);
    }

    public void subtractExperience(UUID memberId, int amount) {
        MemberExperience exp = repository.findByOwnerId(memberId)
                .orElseGet(() -> repository.save(MemberExperience.builder()
                        .ownerId(memberId)
                        .experience(0)
                        .build()));
        exp.subtractExperience(amount);
    }

    @Transactional(readOnly = true)
    public int getExperience(UUID memberId) {
        return repository.findByOwnerId(memberId)
                .map(MemberExperience::getExperience)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public Level getCurrentLevel(UUID memberId) {
        int experience = getExperience(memberId);
        Level level = levelService.getLevelByExperience(experience);
        return level != null ? level : createDefaultLevel();
    }
    
    @Transactional(readOnly = true)
    public MemberLevelResult getMemberLevel(UUID memberId) {
        int experience = getExperience(memberId);
        Level currentLevel = getCurrentLevel(memberId);
        
        Level nextLevel = levelService.getNextLevel(currentLevel.getLevel());
        int experienceToNext = nextLevel != null ? nextLevel.getRequiredExp() - experience : 0;
        
        int currentLevelProgress = experience - currentLevel.getRequiredExp();
        int currentLevelTotal = nextLevel != null ? 
            nextLevel.getRequiredExp() - currentLevel.getRequiredExp() : 0;
        
        return new MemberLevelResult(
                currentLevel.getLevel(),
                currentLevel.getName(),
                experience,
                currentLevel.getRequiredExp(),
                experienceToNext,
                currentLevelProgress,
                currentLevelTotal
        );
    }

    private Level createDefaultLevel() {
        return Level.builder()
                .level(1)
                .name("찍찍 초심자")
                .requiredExp(0)
                .build();
    }
}
