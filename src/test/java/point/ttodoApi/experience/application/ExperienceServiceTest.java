package point.ttodoApi.experience.application;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.ttodoApi.experience.domain.MemberExperience;
import point.ttodoApi.experience.infrastructure.MemberExperienceRepository;
import point.ttodoApi.level.application.LevelService;
import point.ttodoApi.level.domain.Level;
import point.ttodoApi.level.infrastructure.LevelRepository;

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
