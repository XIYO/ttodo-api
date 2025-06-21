package point.zzicback.experience.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import point.zzicback.experience.domain.MemberExperience;
import point.zzicback.experience.infrastructure.MemberExperienceRepository;
import point.zzicback.member.application.event.MemberCreatedEvent;

@Component
@RequiredArgsConstructor
public class ExperienceEventHandler {
    private final MemberExperienceRepository repository;

    @EventListener
    public void handleMemberCreated(MemberCreatedEvent event) {
        repository.findByMemberId(event.memberId())
                .orElseGet(() -> repository.save(MemberExperience.builder()
                        .memberId(event.memberId())
                        .experience(0)
                        .build()));
    }
}
