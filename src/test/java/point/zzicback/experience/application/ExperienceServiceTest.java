package point.zzicback.experience.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.experience.domain.MemberExperience;
import point.zzicback.experience.infrastructure.MemberExperienceRepository;
import point.zzicback.level.application.LevelService;
import point.zzicback.level.domain.Level;
import point.zzicback.level.infrastructure.LevelRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ExperienceService.class, LevelService.class})
class ExperienceServiceTest {

    @Autowired
    private ExperienceService experienceService;
    @Autowired
    private MemberExperienceRepository memberExperienceRepository;
    @Autowired
    private LevelRepository levelRepository;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        levelRepository.save(new Level(1, "Lv1", 0));
        levelRepository.save(new Level(2, "Lv2", 10));
        levelRepository.save(new Level(3, "Lv3", 30));

        memberId = UUID.randomUUID();
        memberExperienceRepository.save(
                MemberExperience.builder()
                        .memberId(memberId)
                        .experience(15)
                        .build()
        );
    }

    @Test
    @DisplayName("회원 경험치 레벨 계산")
    void getMemberLevel() {
        var result = experienceService.getMemberLevel(memberId);

        assertThat(result.currentLevel()).isEqualTo(2);
        assertThat(result.currentExp()).isEqualTo(15);
        assertThat(result.currentLevelMinExp()).isEqualTo(10);
        assertThat(result.currentLevelProgress()).isEqualTo(5);
        assertThat(result.currentLevelTotal()).isEqualTo(20);
        assertThat(result.expToNextLevel()).isEqualTo(15);
    }
}
