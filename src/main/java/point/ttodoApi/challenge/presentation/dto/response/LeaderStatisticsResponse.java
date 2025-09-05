package point.ttodoApi.challenge.presentation.dto.response;

import lombok.*;
import point.ttodoApi.challenge.application.ChallengeLeaderService;

/**
 * 리더 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderStatisticsResponse {
    
    private long currentLeaders;
    private long maxLeaders;
    private boolean canAddMore;
    private double leaderRatio; // 전체 참여자 대비 리더 비율
    
    public static LeaderStatisticsResponse from(ChallengeLeaderService.LeaderStatistics statistics) {
        double ratio = statistics.maxLeaders() > 0 ? 
            (double) statistics.currentLeaders() / statistics.maxLeaders() * 100 : 0;
        
        return LeaderStatisticsResponse.builder()
            .currentLeaders(statistics.currentLeaders())
            .maxLeaders(statistics.maxLeaders())
            .canAddMore(statistics.canAddMore())
            .leaderRatio(Math.round(ratio * 100.0) / 100.0) // 소수점 2자리
            .build();
    }
}