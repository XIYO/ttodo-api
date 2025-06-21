package point.zzicback.experience.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.experience.domain.MemberExperience;
import point.zzicback.experience.infrastructure.MemberExperienceRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExperienceService {
    private final MemberExperienceRepository repository;

    public void addExperience(UUID memberId, int amount) {
        MemberExperience exp = repository.findByMemberId(memberId)
                .orElseGet(() -> repository.save(MemberExperience.builder()
                        .memberId(memberId)
                        .experience(0)
                        .build()));
        exp.addExperience(amount);
    }

    @Transactional(readOnly = true)
    public int getExperience(UUID memberId) {
        return repository.findByMemberId(memberId)
                .map(MemberExperience::getExperience)
                .orElse(0);
    }
}
