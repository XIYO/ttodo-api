package point.zzicback.service;

import point.zzicback.domain.Todo;

import java.util.List;
import java.util.Optional;

/**
 * 인터페이스 입니다 구현체를 만들어주세요
 */
public interface TodoService {
    List<Todo> findAll();
    Optional<Todo> findById(Long id);
    void save(Todo todo);
    void deleteById(Long id);
}
