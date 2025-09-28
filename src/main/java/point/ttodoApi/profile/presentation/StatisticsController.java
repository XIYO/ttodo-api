package point.ttodoApi.profile.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.profile.application.StatisticsService;
import point.ttodoApi.profile.domain.Statistics;
import point.ttodoApi.profile.presentation.dto.response.StatisticsResponse;

import java.util.UUID;

@Tag(name = "통계(Statistics)", description = "사용자의 할 일 관리 활동에 대한 다양한 통계 정보를 제공합니다. 완료한 할 일 수, 생성한 카테고리 수 등의 통계를 확인할 수 있습니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/{userId}/profile")
public class StatisticsController {

  private final StatisticsService statisticsService;

  @Operation(
          summary = "사용자 활동 통계 조회",
          description = "사용자의 할 일 관리 활동에 대한 종합 통계를 조회합니다. 본인만 조회 가능합니다.\n\n" +
                  "통계 항목:\n" +
                  "- succeededTodosCount: 지금까지 완료한 총 할 일 수\n" +
                  "- categoryCount: 생성한 카테고리 수"
  )
  @ApiResponse(responseCode = "200", description = "통계 조회 성공")
  @ApiResponse(responseCode = "403", description = "다른 사용자의 통계 조회 시도")
  @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
  @PreAuthorize("#userId == authentication.principal.id")
  @GetMapping("/statistics")
  public StatisticsResponse getStatistics(@PathVariable UUID userId) {
    Statistics statistics = statisticsService.getStatistics(userId);

    return new StatisticsResponse(
            statistics.getSucceededTodosCount(),
            statistics.getCategoryCount()
    );
  }
}
