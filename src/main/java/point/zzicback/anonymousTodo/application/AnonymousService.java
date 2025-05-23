package point.zzicback.anonymousTodo.application;

import point.zzicback.anonymousTodo.domain.AnonymousTodo;

import java.util.List;

public interface AnonymousService {
    AnonymousTodo createTodo(String guestId, String content);

    List<AnonymousTodo> getTodos(String guestId);

    void updateTodo(Long id, String guestId, String content, boolean done);

    void deleteTodo(Long id, String guestId);
}
