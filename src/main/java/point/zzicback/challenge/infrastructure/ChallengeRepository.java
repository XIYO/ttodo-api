package point.zzicback.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import point.zzicback.challenge.domain.Challenge;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    /**
     * 모든 챌린지와 참여자 정보를 함께 조회
     * N+1 문제를 방지하기 위해 fetch join 사용
     */
    @Query("SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.participations p LEFT JOIN FETCH p.member")
    List<Challenge> findAllWithParticipations();
}
