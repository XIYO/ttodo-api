package point.ttodoApi.shared.config.level;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.level.domain.Level;
import point.ttodoApi.level.infrastructure.LevelRepository;

import java.util.List;

/**
 * 레벨 시스템 초기화 담당
 * 애플리케이션 시작 시 레벨 데이터가 없으면 기본 레벨 20개를 생성
 * 실행 순서: 1번 (가장 먼저 실행)
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class LevelInitializer implements ApplicationRunner {

  private final LevelRepository levelRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    initialize();
  }

  /**
   * 레벨 시스템 초기화
   *
   * @return 초기화 성공 여부
   */
  private boolean initialize() {
    log.info("Initializing levels...");

    if (levelRepository.count() > 0) {
      log.info("Levels already exist, skipping level initialization");
      return false;
    }

    List<Level> levels = createDefaultLevels();
    levelRepository.saveAll(levels);

    log.info("Initialized {} levels", levels.size());
    return true;
  }

  /**
   * 기본 레벨 목록 생성
   *
   * @return 레벨 목록
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