package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoRepository;

import java.util.*;

/**
 * JPA를 이용한 TodoRepository 구현체
 * Infrastructure 계층에 위치하여 기술적 구현사항을 담당
 */
@Repository
public interface JpaTodoRepository extends JpaRepository<Todo, Long>, TodoRepository {
    // JpaRepository에서 기본 CRUD 메서드들을 상속받고
    // TodoRepository에서 도메인 특화 메서드들을 상속받음
    
    // 추가적인 JPA 특화 쿼리 메서드들이 필요하면 여기에 정의
}
