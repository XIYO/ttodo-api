package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.todo.domain.TodoOriginal;

import java.util.*;

public interface TodoOriginalRepository extends JpaRepository<TodoOriginal, Long> {
    
    @Query("SELECT t FROM TodoOriginal t WHERE t.member.id = :memberId AND t.active = true")
    List<TodoOriginal> findByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT t FROM TodoOriginal t WHERE t.id = :todoOriginalId AND t.member.id = :memberId AND t.active = true")
    Optional<TodoOriginal> findByIdAndMemberId(@Param("todoOriginalId") Long todoOriginalId, @Param("memberId") UUID memberId);
    
    @Query("SELECT DISTINCT tag FROM TodoOriginal t JOIN t.tags tag WHERE t.member.id = :memberId " +
           "AND t.active = true " +
           "AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)")
    Page<String> findDistinctTagsByMemberId(@Param("memberId") UUID memberId,
                                            @Param("categoryIds") List<UUID> categoryIds,
                                            Pageable pageable);
    
    @Query("SELECT t FROM TodoOriginal t WHERE t.member.id = :memberId AND t.isPinned = true AND t.active = true ORDER BY t.displayOrder ASC")
    List<TodoOriginal> findByMemberIdAndIsPinnedTrueOrderByDisplayOrderAsc(@Param("memberId") UUID memberId);
}
