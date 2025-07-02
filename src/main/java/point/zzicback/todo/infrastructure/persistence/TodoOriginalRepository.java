package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.TodoOriginal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoOriginalRepository extends JpaRepository<TodoOriginal, Long> {
    
    List<TodoOriginal> findByMemberId(UUID memberId);
    
    Optional<TodoOriginal> findByIdAndMemberId(Long todoOriginalId, UUID memberId);
    
    @Query("SELECT DISTINCT tag FROM TodoOriginal t JOIN t.tags tag WHERE t.member.id = :memberId " +
           "AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)")
    Page<String> findDistinctTagsByMemberId(@Param("memberId") UUID memberId,
                                            @Param("categoryIds") List<Long> categoryIds,
                                            Pageable pageable);
}
