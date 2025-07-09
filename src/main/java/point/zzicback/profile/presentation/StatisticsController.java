package point.zzicback.profile.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import point.zzicback.profile.application.StatisticsService;
import point.zzicback.profile.domain.Statistics;
import point.zzicback.profile.presentation.dto.response.StatisticsResponse;

import java.util.UUID;

@Tag(name = "사용자 통계", description = "완료한 한일 , 카테고리 수 등 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/profile")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "사용자 통계 조회", description = "완료한 할일 수와 생성한 카테고리 수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "통계 조회 성공")
    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않습니다.")
    @PreAuthorize("#memberId == authentication.principal.id")
    @GetMapping("/statistics")
    public StatisticsResponse getStatistics(@PathVariable UUID memberId) {
        Statistics statistics = statisticsService.getStatistics(memberId);

        return new StatisticsResponse(
                statistics.getSucceededTodosCount(),
                statistics.getCategoryCount()
        );
    }
}
