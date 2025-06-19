package point.zzicback.member.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import point.zzicback.category.application.CategoryService;
import point.zzicback.category.application.command.CreateCategoryCommand;

@Component
@RequiredArgsConstructor
public class MemberEventHandler {
    private final CategoryService categoryService;

    @EventListener
    public void handleMemberCreated(MemberCreatedEvent event) {
        CreateCategoryCommand command = new CreateCategoryCommand(event.memberId(), "기본");
        categoryService.createCategory(command);
    }
}
