package point.zzicback.todo.domain;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;

/**
 * Todo 도메인을 위한 Repository 인터페이스
 * DDD 원칙에 따라 도메인 계층에 위치
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
    Page<Todo> findByMemberIdAndStatus(UUID memberId, TodoStatus status, Pageable pageable);
    Page<Todo> findByMemberId(UUID memberId, Pageable pageable);
    Optional<Todo> findByIdAndMemberId(Long todoId, UUID memberId);
    long countByMemberId(UUID memberId);

    @Query("SELECT t FROM Todo t WHERE t.member.id = :memberId AND t.status = 'IN_PROGRESS' AND t.dueDate < :currentDate")
    Page<Todo> findOverdueTodos(@Param("memberId") UUID memberId, @Param("currentDate") LocalDate currentDate, Pageable pageable);
}

