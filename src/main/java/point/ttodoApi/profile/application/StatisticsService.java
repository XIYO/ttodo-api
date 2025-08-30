package point.ttodoApi.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.application.CategoryService;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.domain.Member;
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
    private final CategoryService categoryService;
    private final MemberService memberService;

    /**
     * 사용자 통계 조회
     */
    @Transactional
    public Statistics getStatistics(UUID memberId) {
        // 실시간으로 완료한 할일 수와 카테고리 수를 계산
        long completedTodos = todoTemplateService.countCompletedTodos(memberId);
        long totalCategories = categoryService.countByOwnerId(memberId);

        // Statistics 엔티티에 저장/업데이트
        updateStatisticsEntity(memberId, (int) completedTodos, (int) totalCategories);

        return statisticsRepository.findByOwnerId(memberId)
                .orElseThrow(() -> new IllegalStateException("Statistics not found"));
    }

    /**
     * Statistics 엔티티 업데이트
     */
    private void updateStatisticsEntity(UUID memberId, int completedTodos, int totalCategories) {
        Statistics statistics = statisticsRepository.findByOwnerId(memberId)
                .orElseGet(() -> {
                    Member member = memberService.findByIdOrThrow(memberId);
                    return Statistics.builder()
                            .owner(member)
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
