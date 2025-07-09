package point.ttodoApi.experience.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import point.ttodoApi.experience.application.ExperienceService;
import point.ttodoApi.experience.domain.MemberExperience;
import point.ttodoApi.experience.infrastructure.MemberExperienceRepository;
import point.ttodoApi.member.application.event.MemberCreatedEvent;

@Component
@RequiredArgsConstructor
public class ExperienceEventHandler {
    private final MemberExperienceRepository repository;
    private final ExperienceService experienceService;

    @EventListener
    public void handleMemberCreated(MemberCreatedEvent event) {
        repository.findByMemberId(event.memberId())
                .orElseGet(() -> repository.save(MemberExperience.builder()
                        .memberId(event.memberId())
                        .experience(0)
                        .build()));
    }

    @EventListener
    public void handleTodoCompleted(TodoCompletedEvent event) {
        experienceService.addExperience(event.memberId(), 10);
    }

    @EventListener
    public void handleTodoUncompleted(TodoUncompletedEvent event) {
        experienceService.subtractExperience(event.memberId(), 10);
    }

    @EventListener
    public void handleChallengeTodoCompleted(ChallengeTodoCompletedEvent event) {
        experienceService.addExperience(event.memberId(), 20);
    }
}
