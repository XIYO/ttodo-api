package point.ttodoApi.shared.bootstrap.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import point.ttodoApi.level.domain.Level;
import point.ttodoApi.level.infrastructure.LevelRepository;

import java.util.List;

/**
 * 레벨 시스템 초기 데이터 생성
 * 레벨 1-20 기본 데이터 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LevelDataBootstrap {

    private final LevelRepository levelRepository;

    public void initialize() {
        if (levelRepository.count() > 0) {
            log.info("Levels already exist, skipping level initialization");
            return;
        }

        List<Level> levels = createDefaultLevels();
        levelRepository.saveAll(levels);
        
        log.info("Initialized {} levels", levels.size());
    }

    /**
     * 기본 레벨 목록 생성 (1-20)
     */
    private List<Level> createDefaultLevels() {
        return List.of(
                Level.builder().level(1).name("떠오르는 새싹").requiredExp(0).build(),
                Level.builder().level(2).name("작은 불꽃").requiredExp(100).build(),
                Level.builder().level(3).name("흔들리는 나무").requiredExp(250).build(),
                Level.builder().level(4).name("단단한 바위").requiredExp(450).build(),
                Level.builder().level(5).name("흐르는 강물").requiredExp(700).build(),
                Level.builder().level(6).name("빛나는 별빛").requiredExp(1000).build(),
                Level.builder().level(7).name("고요한 달빛").requiredExp(1350).build(),
                Level.builder().level(8).name("믿음의 등대").requiredExp(1750).build(),
                Level.builder().level(9).name("불타는 열정").requiredExp(2200).build(),
                Level.builder().level(10).name("깨어난 거인").requiredExp(2700).build(),
                Level.builder().level(11).name("날개 단 독수리").requiredExp(3250).build(),
                Level.builder().level(12).name("폭풍의 항해사").requiredExp(3850).build(),
                Level.builder().level(13).name("번개의 주인").requiredExp(4500).build(),
                Level.builder().level(14).name("시간의 지배자").requiredExp(5200).build(),
                Level.builder().level(15).name("운명의 개척자").requiredExp(5950).build(),
                Level.builder().level(16).name("지혜의 현자").requiredExp(6750).build(),
                Level.builder().level(17).name("빛의 수호자").requiredExp(7600).build(),
                Level.builder().level(18).name("차원의 여행자").requiredExp(8500).build(),
                Level.builder().level(19).name("영원의 불꽃").requiredExp(9450).build(),
                Level.builder().level(20).name("시공의 초월자").requiredExp(10450).build()
        );
    }
}