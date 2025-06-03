package point.zzicback.todo.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

/**
 * Todo 도메인을 위한 Repository 인터페이스
 * DDD 원칙에 따라 도메인 계층에 위치
 */
public interface TodoRepository {
    Page<Todo> findByMemberIdAndDone(UUID memberId, Boolean done, Pageable pageable);
    Optional<Todo> findByIdAndMemberId(Long todoId, UUID memberId);
    Optional<Todo> findById(Long id);
    Todo save(Todo todo);
    void deleteById(Long id);
}
