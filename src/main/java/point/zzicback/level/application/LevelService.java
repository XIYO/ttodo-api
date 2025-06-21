package point.zzicback.level.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.level.domain.Level;
import point.zzicback.level.infrastructure.LevelRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LevelService {
    private final LevelRepository levelRepository;

    public Level getLevelByExperience(int experience) {
        return levelRepository.findAll().stream()
                .filter(l -> experience >= l.getRequiredExp())
                .max(java.util.Comparator.comparingInt(Level::getRequiredExp))
                .orElse(null);
    }
}
