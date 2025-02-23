package point.zzicback.service;

import point.zzicback.domain.Zzic;

import java.util.List;
import java.util.Optional;

/**
 * 인터페이스 입니다 구현체를 만들어주세요
 */
public interface ZzicService {
    List<Zzic> findAll();
    Optional<Zzic> findById(Long id);
    void save(Zzic zzic);
    void deleteById(Long id);
}
