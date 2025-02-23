package point.zzicback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.domain.Zzic;
import point.zzicback.repository.ZzicRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ZzicServiceImpl implements ZzicService {

    private final ZzicRepository zzicRepository;

    @Override
    public List<Zzic> findAll() {
        return this.zzicRepository.findAll();
    }

    @Override
    public Optional<Zzic> findById(Long id) {
        return this.zzicRepository.findById(id);
    }

    @Override
    public void save(Zzic zzic) {
        this.zzicRepository.save(zzic);
    }

    @Override
    public void deleteById(Long id) {
        this.zzicRepository.deleteById(id);
    }
}
