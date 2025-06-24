package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.RepeatTodo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepeatTodoRepository extends JpaRepository<RepeatTodo, Long> {
    
    List<RepeatTodo> findByMemberIdAndIsActiveTrue(UUID memberId);
    
    Optional<RepeatTodo> findByIdAndMemberId(Long id, UUID memberId);
    
    Optional<RepeatTodo> findByTodoId(Long todoId);
    
    @Query("SELECT rt FROM RepeatTodo rt WHERE rt.member.id = :memberId " +
           "AND rt.isActive = true " +
           "AND rt.repeatEndDate >= :currentDate")
    List<RepeatTodo> findActiveRepeatTodosByMemberIdAndEndDateAfter(
            @Param("memberId") UUID memberId, 
            @Param("currentDate") LocalDate currentDate);
    
    boolean existsByTodoIdAndMemberId(Long todoId, UUID memberId);
    
    void deleteByTodoId(Long todoId);
}
