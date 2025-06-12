package point.zzicback.challenge.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import point.zzicback.challenge.domain.Challenge;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    @Query(value = "SELECT c.* FROM challenge c LEFT JOIN challenge_participation p ON c.id = p.challenge_id AND p.join_out IS NULL " +
           "GROUP BY c.id " +
           "ORDER BY COUNT(p.id) DESC, c.start_date DESC", 
           nativeQuery = true)
    List<Challenge> findAllOrderedByPopularityNative(); // 네이티브 쿼리로 인기 챌린지 조회 

    @Query("SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.participations p LEFT JOIN FETCH p.member")
    List<Challenge> findAllWithParticipations();

    @Query(value = "SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.participations p LEFT JOIN FETCH p.member",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Challenge c")
    Page<Challenge> findAllWithParticipations(Pageable pageable);

    @Query("SELECT c FROM Challenge c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Challenge> searchByKeyword(String keyword, Pageable pageable);

    @Query("SELECT c FROM Challenge c LEFT JOIN c.participations p " +
           "GROUP BY c " +
           "ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC, c.startDate DESC")
    Page<Challenge> findAllOrderedByPopularity(Pageable pageable);

    @Query("SELECT c FROM Challenge c LEFT JOIN c.participations p " +
           "WHERE (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "GROUP BY c " +
           "ORDER BY COUNT(CASE WHEN p.joinOut IS NULL THEN 1 ELSE NULL END) DESC, c.startDate DESC")
    Page<Challenge> searchByKeywordOrderedByPopularity(String keyword, Pageable pageable);
}
