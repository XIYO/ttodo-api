package point.zzicback.level.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import point.zzicback.level.domain.Level;
import point.zzicback.level.infrastructure.LevelRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LevelInitializer implements ApplicationRunner {
    private final LevelRepository levelRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (levelRepository.count() > 0) {
            log.info("Levels already initialized");
            return;
        }
        List<Level> levels = List.of(
                new Level(1, "찍찍 초심자", 0),
                new Level(2, "나도 J?", 100),
                new Level(3, "혹시 내가 J?", 250),
                new Level(4, "작심삼일 브레이커", 450),
                new Level(5, "나무늘보", 700),
                new Level(6, "미라클모닝러", 1000),
                new Level(7, "밤샘요정", 1350),
                new Level(8, "약속지키미", 1750),
                new Level(9, "꾸준찍찍이", 2200),
                new Level(10, "파워J", 2700),
                new Level(11, "찍찍 숙련자", 3250),
                new Level(12, "도전러", 3850),
                new Level(13, "극한 찍찍이", 4500),
                new Level(14, "꾸준왕", 5200),
                new Level(15, "헬다이브 준비생", 5950),
                new Level(16, "헬다이브 베테랑", 6750),
                new Level(17, "헬다이브 마스터", 7600),
                new Level(18, "지옥의 사령관", 8500),
                new Level(19, "찍찍 챌린저", 9450),
                new Level(20, "찍찍 레전드", 10450)
        );
        levelRepository.saveAll(levels);
        log.info("Initialized {} levels", levels.size());
    }
}
