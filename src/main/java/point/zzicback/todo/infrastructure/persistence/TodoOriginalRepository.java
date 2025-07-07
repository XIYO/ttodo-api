package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.TodoOriginal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoOriginalRepository extends JpaRepository<TodoOriginal, Long> {
    
    @EntityGraph(attributePaths = {"category", "member", "tags", "daysOfWeek"})
    @Query("SELECT t FROM TodoOriginal t WHERE t.member.id = :memberId AND t.active = true")
    List<TodoOriginal> findByMemberId(@Param("memberId") UUID memberId);
    
    @EntityGraph(attributePaths = {"category", "member", "tags", "daysOfWeek"})
    @Query("SELECT t FROM TodoOriginal t WHERE t.id = :todoOriginalId AND t.member.id = :memberId AND t.active = true")
    Optional<TodoOriginal> findByIdAndMemberId(@Param("todoOriginalId") Long todoOriginalId, @Param("memberId") UUID memberId);
    
    @Query("SELECT DISTINCT tag FROM TodoOriginal t JOIN t.tags tag WHERE t.member.id = :memberId " +
           "AND t.active = true " +
           "AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)")
    Page<String> findDistinctTagsByMemberId(@Param("memberId") UUID memberId,
                                            @Param("categoryIds") List<Long> categoryIds,
                                            Pageable pageable);
    
    @EntityGraph(attributePaths = {"category", "member", "tags", "daysOfWeek"})
    @Query("SELECT t FROM TodoOriginal t WHERE t.member.id = :memberId AND t.isPinned = true AND t.active = true ORDER BY t.displayOrder ASC")
    List<TodoOriginal> findByMemberIdAndIsPinnedTrueOrderByDisplayOrderAsc(@Param("memberId") UUID memberId);
}
