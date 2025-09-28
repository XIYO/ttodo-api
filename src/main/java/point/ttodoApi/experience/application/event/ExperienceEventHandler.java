package point.ttodoApi.experience.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.experience.application.ExperienceService;
import point.ttodoApi.experience.domain.UserExperience;
import point.ttodoApi.experience.infrastructure.UserExperienceRepository;
import point.ttodoApi.user.application.event.UserCreatedEvent;

@Component
@RequiredArgsConstructor
public class ExperienceEventHandler {
  private final UserExperienceRepository repository;
  private final ExperienceService experienceService;

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleUserCreated(UserCreatedEvent event) {
    repository.findByOwnerId(event.userId())
            .orElseGet(() -> repository.save(UserExperience.builder()
                    .ownerId(event.userId())
                    .experience(0)
                    .build()));
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleTodoCompleted(TodoCompletedEvent event) {
    experienceService.addExperience(event.userId(), 10);
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleTodoUncompleted(TodoUncompletedEvent event) {
    experienceService.subtractExperience(event.userId(), 10);
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleChallengeTodoCompleted(ChallengeTodoCompletedEvent event) {
    experienceService.addExperience(event.userId(), 20);
  }
}
