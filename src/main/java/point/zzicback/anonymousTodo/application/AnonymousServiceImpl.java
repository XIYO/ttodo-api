package point.zzicback.anonymousTodo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.anonymousTodo.domain.AnonymousTodo;
import point.zzicback.anonymousTodo.persistance.AnonymousTodoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnonymousServiceImpl implements AnonymousService {

    private final AnonymousTodoRepository anonymousTodoRepository;

    @Override
    public AnonymousTodo createTodo(String guestId, String content) {
        AnonymousTodo todo = new AnonymousTodo();
        todo.setGuestId(guestId);
        todo.setContent(content);
        todo.setDone(false);
        return anonymousTodoRepository.save(todo);
    }

    @Override
    public List<AnonymousTodo> getTodos(String guestId) {
        return anonymousTodoRepository.findAllByGuestId(guestId);
    }

    @Override
    public void updateTodo(Long id, String guestId, String content, boolean done) {
        AnonymousTodo todo = anonymousTodoRepository.findByIdAndGuestId(id, guestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투두 없음"));
        todo.setContent(content);
        todo.setDone(done);
        anonymousTodoRepository.save(todo);
    }

    @Override
    public void deleteTodo(Long id, String guestId) {
        AnonymousTodo todo = anonymousTodoRepository.findByIdAndGuestId(id, guestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투두 없음"));
        anonymousTodoRepository.delete(todo);
    }
}
