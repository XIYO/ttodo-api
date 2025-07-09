package point.ttodoApi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import point.ttodoApi.profile.domain.Statistics;

import java.util.Optional;
import java.util.UUID;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    
    /**
     * 사용자 ID로 통계 조회
     */
    Optional<Statistics> findByMemberId(UUID memberId);
    
    /**
     * 사용자 ID로 통계 존재 여부 확인
     */
    boolean existsByMemberId(UUID memberId);
    
    /**
     * 사용자 ID로 통계 삭제
     */
    void deleteByMemberId(UUID memberId);
}
