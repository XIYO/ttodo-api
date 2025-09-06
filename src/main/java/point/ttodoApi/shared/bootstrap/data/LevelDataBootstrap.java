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
                new Level(1, "떠오르는 새싹", 0),
                new Level(2, "작은 불꽃", 100),
                new Level(3, "흔들리는 나무", 250),
                new Level(4, "단단한 바위", 450),
                new Level(5, "흐르는 강물", 700),
                new Level(6, "빛나는 별빛", 1000),
                new Level(7, "고요한 달빛", 1350),
                new Level(8, "믿음의 등대", 1750),
                new Level(9, "불타는 열정", 2200),
                new Level(10, "깨어난 거인", 2700),
                new Level(11, "날개 단 독수리", 3250),
                new Level(12, "폭풍의 항해사", 3850),
                new Level(13, "번개의 주인", 4500),
                new Level(14, "시간의 지배자", 5200),
                new Level(15, "운명의 개척자", 5950),
                new Level(16, "지혜의 현자", 6750),
                new Level(17, "빛의 수호자", 7600),
                new Level(18, "차원의 여행자", 8500),
                new Level(19, "영원의 불꽃", 9450),
                new Level(20, "시공의 초월자", 10450)
        );
    }
}