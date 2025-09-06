package point.ttodoApi.shared.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.shared.bootstrap.data.ChallengeDataBootstrap;
import point.ttodoApi.shared.bootstrap.data.LevelDataBootstrap;
import point.ttodoApi.shared.bootstrap.data.MemberDataBootstrap;
import point.ttodoApi.shared.bootstrap.data.TodoDataBootstrap;

/**
 * 애플리케이션 데이터 초기화 담당
 * 시스템 시작 시 필요한 기본 데이터들을 순서대로 생성
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final LevelDataBootstrap levelBootstrap;
    private final MemberDataBootstrap memberBootstrap;
    private final TodoDataBootstrap todoBootstrap;
    private final ChallengeDataBootstrap challengeBootstrap;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting application data initialization...");

        try {
            // 순서대로 데이터 초기화 실행
            levelBootstrap.initialize();      // 1. 레벨 데이터 (기본 레벨 1-20)
            memberBootstrap.initialize();     // 2. 시스템 사용자 (anon, root)
            todoBootstrap.initialize();       // 3. 기본 할일 템플릿
            challengeBootstrap.initialize();  // 4. 기본 챌린지

            log.info("Application data initialization completed successfully");
        } catch (Exception e) {
            log.error("Data initialization failed", e);
            throw e;
        }
    }
}