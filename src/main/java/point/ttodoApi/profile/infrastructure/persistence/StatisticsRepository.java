package point.ttodoApi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.profile.domain.Statistics;

import java.util.*;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    
    /**
     * 사용자 ID로 통계 조회
     */
    @Query("SELECT s FROM Statistics s WHERE s.owner.id = :ownerId")
    Optional<Statistics> findByOwnerId(@Param("ownerId") UUID ownerId);
    
    /**
     * 사용자 ID로 통계 존재 여부 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM Statistics s WHERE s.owner.id = :ownerId")
    boolean existsByOwnerId(@Param("ownerId") UUID ownerId);
    
    /**
     * 사용자 ID로 통계 삭제
     */
    @Modifying
    @Query("DELETE FROM Statistics s WHERE s.owner.id = :ownerId")
    void deleteByOwnerId(@Param("ownerId") UUID ownerId);
}
