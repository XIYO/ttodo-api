package point.ttodoApi.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.application.CategoryQueryService;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.profile.domain.Statistics;
import point.ttodoApi.profile.infrastructure.persistence.StatisticsRepository;
import point.ttodoApi.todo.application.TodoTemplateService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

  private final StatisticsRepository statisticsRepository;
  private final TodoTemplateService todoTemplateService;
  private final CategoryQueryService categoryQueryService; // TTODO: Query 처리
  private final UserService UserService;

  /**
   * 사용자 통계 조회
   */
  @Transactional
  public Statistics getStatistics(UUID userId) {
    // 실시간으로 완료한 할일 수와 카테고리 수를 계산
    long completedTodos = todoTemplateService.countCompletedTodos(userId);
    // TTODO 아키텍처 패턴: Query 서비스 사용
    long totalCategories = categoryQueryService.countByOwnerId(userId);

    // Statistics 엔티티에 저장/업데이트
    updateStatisticsEntity(userId, (int) completedTodos, (int) totalCategories);

    return statisticsRepository.findByOwnerId(userId)
            .orElseThrow(() -> new IllegalStateException("Statistics not found"));
  }

  /**
   * Statistics 엔티티 업데이트
   */
  private void updateStatisticsEntity(UUID userId, int completedTodos, int totalCategories) {
    Statistics statistics = statisticsRepository.findByOwnerId(userId)
            .orElseGet(() -> {
              User user = UserService.findByIdOrThrow(userId);
              return Statistics.builder()
                      .owner(user)
                      .succeededTodosCount(completedTodos)
                      .categoryCount(totalCategories)
                      .build();
            });

    // 기존 Statistics가 있다면 업데이트
    if (statistics.getId() != null) {
      statistics.updateStatistics(completedTodos, totalCategories);
    }

    statisticsRepository.save(statistics);
  }
}
