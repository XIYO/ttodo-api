package point.zzicback.anonymous;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.anonymous.domain.Anonymous;
import point.zzicback.anonymous.persistance.AnonymousRepository;

@Service
@RequiredArgsConstructor
public class AnonymousService {

    private final AnonymousRepository repository;

    public Anonymous signUp(String email, String nickname, String password) {
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        Anonymous anonymous = new Anonymous();
        anonymous.setEmail(email);
        anonymous.setNickname(nickname);
        anonymous.setPassword(password);
        return repository.save(anonymous);
    }
}